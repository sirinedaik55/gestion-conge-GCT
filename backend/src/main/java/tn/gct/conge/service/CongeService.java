package tn.gct.conge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.gct.conge.dto.DemandeCongeDto;
import tn.gct.conge.dto.DemandeCongeRequest;
import tn.gct.conge.dto.ValidationActionRequest;
import tn.gct.conge.entity.*;
import tn.gct.conge.exception.BusinessException;
import tn.gct.conge.exception.ConflitDepartementException;
import tn.gct.conge.exception.QuotaDepasseException;
import tn.gct.conge.exception.ResourceNotFoundException;
import tn.gct.conge.repository.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CongeService {

    private final DemandeCongeRepository demandeCongeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final TypeCongeRepository typeCongeRepository;
    private final SoldeCongeRepository soldeCongeRepository;
    private final HistoriqueValidationRepository historiqueValidationRepository;
    private final SoldeCongeService soldeCongeService;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Value("${app.conge.max-absences-departement:2}")
    private int maxAbsencesDepartementDefault;

    @Value("${app.conge.delai-min-demande-jours:2}")
    private int delaiMinDemandeJours;

    @Transactional
    public DemandeCongeDto soumettreDemande(Long employeId, DemandeCongeRequest request) {
        Utilisateur employe = utilisateurRepository.findById(employeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé"));

        TypeConge typeConge = typeCongeRepository.findById(request.getTypeCongeId())
                .orElseThrow(() -> new ResourceNotFoundException("Type de congé non trouvé"));

        LocalDate dateDebut = request.getDateDebut();
        LocalDate dateFin = request.getDateFin();

        if (dateFin.isBefore(dateDebut)) {
            throw new BusinessException("La date de fin ne peut pas précéder la date de début");
        }

        // Règle 1: Pas de double demande qui se chevauche pour le même employé
        if (demandeCongeRepository.hasOverlappingDemandeForEmploye(employeId, dateDebut, dateFin)) {
            throw new BusinessException("Vous avez déjà une demande de congé en cours ou approuvée sur cette même période");
        }

        // Calcul du nombre de jours ouvrés (hors week-ends)
        int nbJours = calculerJoursOuvres(dateDebut, dateFin);
        if (nbJours <= 0) {
            throw new BusinessException("La période sélectionnée ne comporte aucun jour ouvré");
        }

        int annee = dateDebut.getYear();

        // Règle 2: Vérification du solde disponible
        SoldeConge solde = soldeCongeRepository.findByEmployeIdAndTypeCongeIdAndAnnee(employeId, typeConge.getId(), annee)
                .orElseThrow(() -> new QuotaDepasseException("Aucun solde alloué pour ce type de congé sur l'année " + annee));

        if (solde.getJoursRestants() < nbJours) {
            throw new QuotaDepasseException(String.format(
                    "Solde insuffisant: vous demandez %d jour(s) alors qu'il vous reste %d jour(s) pour le congé %s",
                    nbJours, solde.getJoursRestants(), typeConge.getNom()
            ));
        }

        // Règle 3: Règle de non-chevauchement au sein du même département (max 2 absences simultanées)
        // Exception: Le congé de maternité est légalement prioritaire et ne peut pas être bloqué par quota département
        if (!Boolean.TRUE.equals(typeConge.getAutoApprouve()) && employe.getDepartement() != null) {
            Departement dept = employe.getDepartement();
            int maxAbsences = dept.getMaxAbsencesSimultanees() != null ? dept.getMaxAbsencesSimultanees() : maxAbsencesDepartementDefault;
            long absencesEnCours = demandeCongeRepository.countConcurrentAbsencesInDepartement(dept.getId(), employeId, dateDebut, dateFin);

            if (absencesEnCours >= maxAbsences) {
                throw new ConflitDepartementException(String.format(
                        "Règle GCT: Le quota maximum de %d personne(s) simultanément en congé dans le département '%s' est déjà atteint pour cette période.",
                        maxAbsences, dept.getNom()
                ));
            }
        }

        DemandeConge demande = DemandeConge.builder()
                .employe(employe)
                .typeConge(typeConge)
                .dateDebut(dateDebut)
                .dateFin(dateFin)
                .nbJours(nbJours)
                .motif(request.getMotif())
                .justificatifUrl(request.getJustificatifUrl())
                .build();

        // RÈGLE SPÉCIALE GCT: Congé de Maternité accepté automatiquement sans validation ni attente RH !
        if (Boolean.TRUE.equals(typeConge.getAutoApprouve())) {
            demande.setStatut(StatutDemande.APPROUVE);
            demande.setDateValidationRh(LocalDateTime.now());
            DemandeConge saved = demandeCongeRepository.save(demande);

            // Déduction immédiate du solde
            soldeCongeService.deduireJours(employeId, typeConge.getId(), nbJours, annee);

            // Enregistrer l'historique
            enregistrerHistorique(saved, null, "AUTO_APPROBATION", null, StatutDemande.APPROUVE.name(),
                    "Congé de maternité approuvé automatiquement selon le règlement GCT.");

            // Notifications
            notificationService.envoyerNotification(
                    employe,
                    "Congé de maternité approuvé",
                    String.format("Votre congé de maternité du %s au %s (%d jours) a été approuvé automatiquement.", dateDebut, dateFin, nbJours),
                    "APPROBATION_AUTOMATIQUE",
                    saved
            );

            emailService.envoyerEmail(
                    employe.getEmail(),
                    "Confirmation de votre congé de maternité",
                    String.format("Bonjour %s,\n\nVotre demande de congé de maternité pour la période du %s au %s (%d jours) a été validée et enregistrée automatiquement.\n\nCordialement,\nService RH - GCT",
                            employe.getPrenom(), dateDebut, dateFin, nbJours)
            );

            log.info("Congé maternité auto-approuvé pour l'employée ID: {}", employeId);
            return mapToDto(saved);

        } else {
            // Workflow normal: EN_ATTENTE de validation du chef de département
            demande.setStatut(StatutDemande.EN_ATTENTE);
            DemandeConge saved = demandeCongeRepository.save(demande);

            enregistrerHistorique(saved, employe, "SOUMISSION", null, StatutDemande.EN_ATTENTE.name(), "Demande soumise par l'employé.");

            // Notification au chef de département
            if (employe.getDepartement() != null) {
                List<Utilisateur> chefs = utilisateurRepository.findByDepartementIdAndRole(
                        employe.getDepartement().getId(),
                        RoleUtilisateur.CHEF_DEPARTEMENT
                );
                for (Utilisateur chef : chefs) {
                    notificationService.envoyerNotification(
                            chef,
                            "Nouvelle demande de congé à valider",
                            String.format("%s %s a déposé une demande de %s du %s au %s (%d jours).",
                                    employe.getPrenom(), employe.getNom(), typeConge.getNom(), dateDebut, dateFin, nbJours),
                            "NOUVELLE_DEMANDE",
                            saved
                    );
                    emailService.envoyerEmail(
                            chef.getEmail(),
                            "Nouvelle demande de congé à viser",
                            String.format("Bonjour %s,\n\n%s %s a déposé une demande de congé à valider sur le portail GCT.\nDates: du %s au %s (%d jours).\n\nMerci de vous connecter pour statuer.",
                                    chef.getPrenom(), employe.getPrenom(), employe.getNom(), dateDebut, dateFin, nbJours)
                    );
                }
            }

            return mapToDto(saved);
        }
    }

    // Étape 1 du workflow normal: Validation par le Chef de Département
    @Transactional
    public DemandeCongeDto validerParChef(Long demandeId, Long chefId, ValidationActionRequest request) {
        DemandeConge demande = demandeCongeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de congé non trouvée"));

        if (demande.getStatut() != StatutDemande.EN_ATTENTE) {
            throw new BusinessException("Cette demande n'est plus en attente de visa du chef (statut actuel: " + demande.getStatut() + ")");
        }

        Utilisateur chef = utilisateurRepository.findById(chefId)
                .orElseThrow(() -> new ResourceNotFoundException("Chef de département non trouvé"));

        demande.setStatut(StatutDemande.APPROUVE_CHEF);
        demande.setChefValideur(chef);
        demande.setDateValidationChef(LocalDateTime.now());
        DemandeConge updated = demandeCongeRepository.save(demande);

        enregistrerHistorique(updated, chef, "VALIDATION_CHEF", StatutDemande.EN_ATTENTE.name(),
                StatutDemande.APPROUVE_CHEF.name(), request != null ? request.getCommentaire() : "Avis favorable accordé par le chef de département.");

        // Notifier l'employé
        notificationService.envoyerNotification(
                demande.getEmploye(),
                "Avis favorable de votre chef de département",
                "Votre demande de congé a été validée par votre chef et transmise à la Direction RH pour accord final.",
                "VISA_CHEF_ACCORDE",
                updated
        );

        // Notifier les RH
        List<Utilisateur> equipeRh = utilisateurRepository.findByRole(RoleUtilisateur.RH);
        for (Utilisateur rh : equipeRh) {
            notificationService.envoyerNotification(
                    rh,
                    "Demande de congé visée par le chef à valider",
                    String.format("La demande de %s %s (%s) a reçu l'avis favorable de son chef et attend votre validation finale.",
                            demande.getEmploye().getPrenom(), demande.getEmploye().getNom(), demande.getTypeConge().getNom()),
                    "VALIDATION_RH_REQUISE",
                    updated
            );
        }

        return mapToDto(updated);
    }

    // Étape 2 du workflow normal: Validation finale par les Ressources Humaines (RH)
    @Transactional
    public DemandeCongeDto validerParRh(Long demandeId, Long rhId, ValidationActionRequest request) {
        DemandeConge demande = demandeCongeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de congé non trouvée"));

        if (demande.getStatut() != StatutDemande.APPROUVE_CHEF && demande.getStatut() != StatutDemande.EN_ATTENTE) {
            throw new BusinessException("La demande ne peut pas être validée dans son état actuel: " + demande.getStatut());
        }

        Utilisateur rh = utilisateurRepository.findById(rhId)
                .orElseThrow(() -> new ResourceNotFoundException("Responsable RH non trouvé"));

        demande.setStatut(StatutDemande.APPROUVE);
        demande.setRhValideur(rh);
        demande.setDateValidationRh(LocalDateTime.now());
        DemandeConge updated = demandeCongeRepository.save(demande);

        // Déduction définitive des jours du solde
        int annee = demande.getDateDebut().getYear();
        soldeCongeService.deduireJours(demande.getEmploye().getId(), demande.getTypeConge().getId(), demande.getNbJours(), annee);

        enregistrerHistorique(updated, rh, "VALIDATION_FINALE_RH", StatutDemande.APPROUVE_CHEF.name(),
                StatutDemande.APPROUVE.name(), request != null ? request.getCommentaire() : "Validation définitive accordée par la Direction RH.");

        // Notifier l'employé
        notificationService.envoyerNotification(
                demande.getEmploye(),
                "🎉 Demande de congé acceptée définitivement",
                String.format("Votre congé du %s au %s (%d jours) a été approuvé par la Direction RH.",
                        demande.getDateDebut(), demande.getDateFin(), demande.getNbJours()),
                "APPROBATION_FINALE",
                updated
        );

        emailService.envoyerEmail(
                demande.getEmploye().getEmail(),
                "Décision finale - Congé accordé",
                String.format("Bonjour %s,\n\nNous avons le plaisir de vous informer que votre demande de congé du %s au %s (%d jours) a été acceptée définitivement.\n\nCordialement,\nDirection des Ressources Humaines - GCT",
                        demande.getEmploye().getPrenom(), demande.getDateDebut(), demande.getDateFin(), demande.getNbJours())
        );

        return mapToDto(updated);
    }

    // Rejet par Chef ou RH avec motif obligatoire
    @Transactional
    public DemandeCongeDto rejeterDemande(Long demandeId, Long valideurId, ValidationActionRequest request) {
        DemandeConge demande = demandeCongeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée"));

        if (demande.getStatut() == StatutDemande.APPROUVE || demande.getStatut() == StatutDemande.REJETE || demande.getStatut() == StatutDemande.ANNULE) {
            throw new BusinessException("Impossible de rejeter une demande déjà finalisée");
        }

        Utilisateur valideur = utilisateurRepository.findById(valideurId)
                .orElseThrow(() -> new ResourceNotFoundException("Valideur non trouvé"));

        String motifRefus = (request != null && request.getCommentaire() != null && !request.getCommentaire().isBlank())
                ? request.getCommentaire()
                : "Demande non conforme aux nécessités de service.";

        String statutPrecedent = demande.getStatut().name();
        demande.setStatut(StatutDemande.REJETE);
        demande.setCommentaireRefus(motifRefus);
        DemandeConge updated = demandeCongeRepository.save(demande);

        enregistrerHistorique(updated, valideur, "REJET", statutPrecedent, StatutDemande.REJETE.name(), motifRefus);

        notificationService.envoyerNotification(
                demande.getEmploye(),
                "Demande de congé refusée",
                String.format("Votre demande de congé du %s au %s a été refusée. Motif : %s",
                        demande.getDateDebut(), demande.getDateFin(), motifRefus),
                "DEMANDE_REFUSEE",
                updated
        );

        emailService.envoyerEmail(
                demande.getEmploye().getEmail(),
                "Refus de demande de congé",
                String.format("Bonjour %s,\n\nVotre demande de congé pour la période du %s au %s n'a pas pu être acceptée.\nMotif : %s\n\nDirection GCT",
                        demande.getEmploye().getPrenom(), demande.getDateDebut(), demande.getDateFin(), motifRefus)
        );

        return mapToDto(updated);
    }

    // Annulation par l'employé
    @Transactional
    public DemandeCongeDto annulerDemande(Long demandeId, Long employeId) {
        DemandeConge demande = demandeCongeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée"));

        if (!demande.getEmploye().getId().equals(employeId)) {
            throw new BusinessException("Vous n'êtes pas autorisé à annuler cette demande");
        }

        if (demande.getStatut() == StatutDemande.ANNULE || demande.getStatut() == StatutDemande.REJETE) {
            throw new BusinessException("Cette demande est déjà terminée");
        }

        // Si la demande était déjà approuvée, on restitue les jours au solde
        if (demande.getStatut() == StatutDemande.APPROUVE) {
            int annee = demande.getDateDebut().getYear();
            soldeCongeService.restituerJours(employeId, demande.getTypeConge().getId(), demande.getNbJours(), annee);
        }

        String precedent = demande.getStatut().name();
        demande.setStatut(StatutDemande.ANNULE);
        DemandeConge updated = demandeCongeRepository.save(demande);

        enregistrerHistorique(updated, demande.getEmploye(), "ANNULATION", precedent, StatutDemande.ANNULE.name(), "Annulée par l'employé");

        return mapToDto(updated);
    }

    @Transactional(readOnly = true)
    public List<DemandeCongeDto> getDemandesParRole(Utilisateur utilisateur) {
        if (utilisateur.getRole() == RoleUtilisateur.ADMIN || utilisateur.getRole() == RoleUtilisateur.RH) {
            return demandeCongeRepository.findAllByOrderByDateSoumissionDesc().stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        } else if (utilisateur.getRole() == RoleUtilisateur.CHEF_DEPARTEMENT && utilisateur.getDepartement() != null) {
            return demandeCongeRepository.findByEmployeDepartementIdOrderByDateSoumissionDesc(utilisateur.getDepartement().getId()).stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        } else {
            return demandeCongeRepository.findByEmployeIdOrderByDateSoumissionDesc(utilisateur.getId()).stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        }
    }

    @Transactional(readOnly = true)
    public DemandeCongeDto getDemandeById(Long id) {
        DemandeConge demande = demandeCongeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée avec l'ID: " + id));
        return mapToDto(demande);
    }

    @Transactional(readOnly = true)
    public List<DemandeCongeDto> getCalendrierConges(LocalDate debut, LocalDate fin) {
        return demandeCongeRepository.findApprovedForCalendar(debut, fin).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Calcul du nombre de jours ouvrés (exclut Samedi et Dimanche)
    public int calculerJoursOuvres(LocalDate debut, LocalDate fin) {
        int jours = 0;
        LocalDate courante = debut;
        while (!courante.isAfter(fin)) {
            DayOfWeek dow = courante.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                jours++;
            }
            courante = courante.plusDays(1);
        }
        return jours;
    }

    private void enregistrerHistorique(DemandeConge demande, Utilisateur valideur, String action, String statutPrecedent, String nouveauStatut, String commentaire) {
        HistoriqueValidation h = HistoriqueValidation.builder()
                .demande(demande)
                .valideur(valideur)
                .action(action)
                .statutPrecedent(statutPrecedent)
                .nouveauStatut(nouveauStatut)
                .commentaire(commentaire)
                .build();
        historiqueValidationRepository.save(h);
    }

    public DemandeCongeDto mapToDto(DemandeConge d) {
        return DemandeCongeDto.builder()
                .id(d.getId())
                .employeId(d.getEmploye().getId())
                .employeNom(d.getEmploye().getNom())
                .employePrenom(d.getEmploye().getPrenom())
                .employeMatricule(d.getEmploye().getMatricule())
                .departementNom(d.getEmploye().getDepartement() != null ? d.getEmploye().getDepartement().getNom() : "Non affecté")
                .typeCongeId(d.getTypeConge().getId())
                .typeCongeNom(d.getTypeConge().getNom())
                .typeCongeCode(d.getTypeConge().getCode())
                .autoApprouve(d.getTypeConge().getAutoApprouve())
                .dateDebut(d.getDateDebut())
                .dateFin(d.getDateFin())
                .nbJours(d.getNbJours())
                .statut(d.getStatut())
                .motif(d.getMotif())
                .justificatifUrl(d.getJustificatifUrl())
                .commentaireRefus(d.getCommentaireRefus())
                .dateValidationChef(d.getDateValidationChef())
                .chefValideurNom(d.getChefValideur() != null ? d.getChefValideur().getPrenom() + " " + d.getChefValideur().getNom() : null)
                .dateValidationRh(d.getDateValidationRh())
                .rhValideurNom(d.getRhValideur() != null ? d.getRhValideur().getPrenom() + " " + d.getRhValideur().getNom() : null)
                .dateSoumission(d.getDateSoumission())
                .build();
    }
}
