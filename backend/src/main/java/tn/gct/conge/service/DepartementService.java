package tn.gct.conge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.gct.conge.dto.DepartementDto;
import tn.gct.conge.entity.Departement;
import tn.gct.conge.exception.BusinessException;
import tn.gct.conge.exception.ResourceNotFoundException;
import tn.gct.conge.repository.DepartementRepository;
import tn.gct.conge.repository.UtilisateurRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartementService {

    private final DepartementRepository departementRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Transactional(readOnly = true)
    public List<DepartementDto> getAllDepartements() {
        return departementRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartementDto getDepartementById(Long id) {
        Departement dept = departementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Département non trouvé avec l'ID: " + id));
        return mapToDto(dept);
    }

    @Transactional
    public DepartementDto creerDepartement(DepartementDto dto) {
        if (departementRepository.existsByNom(dto.getNom())) {
            throw new BusinessException("Un département avec ce nom existe déjà");
        }
        if (departementRepository.existsByCode(dto.getCode())) {
            throw new BusinessException("Un département avec ce code existe déjà");
        }

        Departement dept = Departement.builder()
                .nom(dto.getNom())
                .code(dto.getCode().toUpperCase())
                .description(dto.getDescription())
                .maxAbsencesSimultanees(dto.getMaxAbsencesSimultanees() != null ? dto.getMaxAbsencesSimultanees() : 2)
                .build();

        return mapToDto(departementRepository.save(dept));
    }

    @Transactional
    public DepartementDto modifierDepartement(Long id, DepartementDto dto) {
        Departement dept = departementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Département non trouvé"));

        dept.setNom(dto.getNom());
        dept.setDescription(dto.getDescription());
        if (dto.getMaxAbsencesSimultanees() != null) {
            dept.setMaxAbsencesSimultanees(dto.getMaxAbsencesSimultanees());
        }

        return mapToDto(departementRepository.save(dept));
    }

    @Transactional
    public void supprimerDepartement(Long id) {
        if (!departementRepository.existsById(id)) {
            throw new ResourceNotFoundException("Département non trouvé");
        }
        long count = utilisateurRepository.findByDepartementId(id).size();
        if (count > 0) {
            throw new BusinessException("Impossible de supprimer ce département car il contient encore des employés affectés");
        }
        departementRepository.deleteById(id);
    }

    private DepartementDto mapToDto(Departement dept) {
        long count = utilisateurRepository.findByDepartementId(dept.getId()).size();
        return DepartementDto.builder()
                .id(dept.getId())
                .nom(dept.getNom())
                .code(dept.getCode())
                .description(dept.getDescription())
                .maxAbsencesSimultanees(dept.getMaxAbsencesSimultanees())
                .totalEmployes(count)
                .build();
    }
}
