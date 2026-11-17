package tn.gct.conge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.gct.conge.dto.SoldeCongeDto;
import tn.gct.conge.entity.SoldeConge;
import tn.gct.conge.entity.TypeConge;
import tn.gct.conge.entity.Utilisateur;
import tn.gct.conge.exception.ResourceNotFoundException;
import tn.gct.conge.repository.SoldeCongeRepository;
import tn.gct.conge.repository.TypeCongeRepository;
import tn.gct.conge.repository.UtilisateurRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SoldeCongeService {

    private final SoldeCongeRepository soldeCongeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final TypeCongeRepository typeCongeRepository;

    @Transactional(readOnly = true)
    public List<SoldeCongeDto> getSoldesEmploye(Long employeId, Integer annee) {
        int anneeRecherche = (annee != null) ? annee : LocalDate.now().getYear();
        return soldeCongeRepository.findByEmployeIdAndAnnee(employeId, anneeRecherche).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deduireJours(Long employeId, Long typeCongeId, int nbJours, int annee) {
        SoldeConge solde = soldeCongeRepository.findByEmployeIdAndTypeCongeIdAndAnnee(employeId, typeCongeId, annee)
                .orElseThrow(() -> new ResourceNotFoundException("Solde non trouvé pour cet employé et type de congé"));

        solde.setJoursPris(solde.getJoursPris() + nbJours);
        solde.recalculerRestants();
        soldeCongeRepository.save(solde);
    }

    @Transactional
    public void restituerJours(Long employeId, Long typeCongeId, int nbJours, int annee) {
        soldeCongeRepository.findByEmployeIdAndTypeCongeIdAndAnnee(employeId, typeCongeId, annee)
                .ifPresent(solde -> {
                    solde.setJoursPris(Math.max(0, solde.getJoursPris() - nbJours));
                    solde.recalculerRestants();
                    soldeCongeRepository.save(solde);
                });
    }

    @Transactional
    public void initialiserSoldesAnnuels(int annee) {
        List<Utilisateur> users = utilisateurRepository.findAll();
        List<TypeConge> types = typeCongeRepository.findAll();

        for (Utilisateur u : users) {
            for (TypeConge t : types) {
                if (!soldeCongeRepository.existsByEmployeIdAndTypeCongeIdAndAnnee(u.getId(), t.getId(), annee)) {
                    SoldeConge s = SoldeConge.builder()
                            .employe(u)
                            .typeConge(t)
                            .annee(annee)
                            .joursAcquis(t.getJoursMaxParAn())
                            .joursPris(0)
                            .joursRestants(t.getJoursMaxParAn())
                            .build();
                    soldeCongeRepository.save(s);
                }
            }
        }
    }

    private SoldeCongeDto mapToDto(SoldeConge s) {
        return SoldeCongeDto.builder()
                .id(s.getId())
                .typeCongeId(s.getTypeConge().getId())
                .typeCongeNom(s.getTypeConge().getNom())
                .typeCongeCode(s.getTypeConge().getCode())
                .annee(s.getAnnee())
                .joursAcquis(s.getJoursAcquis())
                .joursPris(s.getJoursPris())
                .joursRestants(s.getJoursRestants())
                .build();
    }
}
