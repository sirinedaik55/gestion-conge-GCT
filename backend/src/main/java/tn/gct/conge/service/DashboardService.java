package tn.gct.conge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.gct.conge.dto.DashboardStatsDto;
import tn.gct.conge.entity.DemandeConge;
import tn.gct.conge.entity.SoldeConge;
import tn.gct.conge.entity.StatutDemande;
import tn.gct.conge.entity.Utilisateur;
import tn.gct.conge.repository.DepartementRepository;
import tn.gct.conge.repository.DemandeCongeRepository;
import tn.gct.conge.repository.SoldeCongeRepository;
import tn.gct.conge.repository.UtilisateurRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UtilisateurRepository utilisateurRepository;
    private final DepartementRepository departementRepository;
    private final DemandeCongeRepository demandeCongeRepository;
    private final SoldeCongeRepository soldeCongeRepository;

    @Transactional(readOnly = true)
    public DashboardStatsDto getStats(Utilisateur currentUser) {
        long totalEmployes = utilisateurRepository.countByActifTrue();
        long totalDepartements = departementRepository.count();

        long enAttente = demandeCongeRepository.countByStatut(StatutDemande.EN_ATTENTE) +
                demandeCongeRepository.countByStatut(StatutDemande.APPROUVE_CHEF);
        long approuvees = demandeCongeRepository.countByStatut(StatutDemande.APPROUVE);
        long rejetees = demandeCongeRepository.countByStatut(StatutDemande.REJETE);

        // Mon solde restant pour congé annuel
        int monSolde = 0;
        int annee = LocalDate.now().getYear();
        List<SoldeConge> soldes = soldeCongeRepository.findByEmployeIdAndAnnee(currentUser.getId(), annee);
        for (SoldeConge s : soldes) {
            if ("ANNUEL".equalsIgnoreCase(s.getTypeConge().getCode())) {
                monSolde = s.getJoursRestants();
                break;
            }
        }

        // Répartition par type de congé
        Map<String, Long> repartitionType = new HashMap<>();
        List<DemandeConge> toutesLesDemandes = demandeCongeRepository.findAll();
        for (DemandeConge d : toutesLesDemandes) {
            String type = d.getTypeConge().getNom();
            repartitionType.put(type, repartitionType.getOrDefault(type, 0L) + 1);
        }

        // Répartition par département
        Map<String, Long> repartitionDept = new HashMap<>();
        for (DemandeConge d : toutesLesDemandes) {
            if (d.getEmploye().getDepartement() != null) {
                String deptNom = d.getEmploye().getDepartement().getNom();
                repartitionDept.put(deptNom, repartitionDept.getOrDefault(deptNom, 0L) + 1);
            }
        }

        return DashboardStatsDto.builder()
                .totalEmployes(totalEmployes)
                .totalDepartements(totalDepartements)
                .demandesEnAttente(enAttente)
                .demandesApprouvees(approuvees)
                .demandesRejetees(rejetees)
                .monSoldeRestant(monSolde)
                .repartitionParType(repartitionType)
                .repartitionParDepartement(repartitionDept)
                .build();
    }
}
