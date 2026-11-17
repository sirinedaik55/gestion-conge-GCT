package tn.gct.conge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.gct.conge.dto.RegisterRequest;
import tn.gct.conge.dto.UtilisateurDto;
import tn.gct.conge.entity.*;
import tn.gct.conge.exception.BusinessException;
import tn.gct.conge.exception.ResourceNotFoundException;
import tn.gct.conge.repository.DepartementRepository;
import tn.gct.conge.repository.SoldeCongeRepository;
import tn.gct.conge.repository.TypeCongeRepository;
import tn.gct.conge.repository.UtilisateurRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final DepartementRepository departementRepository;
    private final TypeCongeRepository typeCongeRepository;
    private final SoldeCongeRepository soldeCongeRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UtilisateurDto> getAllUtilisateurs() {
        return utilisateurRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UtilisateurDto getUtilisateurById(Long id) {
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé avec l'ID: " + id));
        return mapToDto(user);
    }

    @Transactional(readOnly = true)
    public List<UtilisateurDto> getUtilisateursByDepartement(Long departementId) {
        return utilisateurRepository.findByDepartementId(departementId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UtilisateurDto creerUtilisateur(RegisterRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Cet email est déjà utilisé");
        }
        if (utilisateurRepository.existsByMatricule(request.getMatricule())) {
            throw new BusinessException("Ce matricule est déjà attribué");
        }

        Departement dept = null;
        if (request.getDepartementId() != null) {
            dept = departementRepository.findById(request.getDepartementId())
                    .orElseThrow(() -> new ResourceNotFoundException("Département non trouvé"));
        }

        Utilisateur user = Utilisateur.builder()
                .matricule(request.getMatricule())
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : RoleUtilisateur.EMPLOYE)
                .poste(request.getPoste())
                .telephone(request.getTelephone())
                .departement(dept)
                .actif(true)
                .build();

        Utilisateur saved = utilisateurRepository.save(user);

        // Initialisation soldes
        int annee = LocalDate.now().getYear();
        for (TypeConge t : typeCongeRepository.findAll()) {
            SoldeConge solde = SoldeConge.builder()
                    .employe(saved)
                    .typeConge(t)
                    .annee(annee)
                    .joursAcquis(t.getJoursMaxParAn())
                    .joursPris(0)
                    .joursRestants(t.getJoursMaxParAn())
                    .build();
            soldeCongeRepository.save(solde);
        }

        return mapToDto(saved);
    }

    @Transactional
    public UtilisateurDto modifierUtilisateur(Long id, RegisterRequest request) {
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé"));

        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setPoste(request.getPoste());
        user.setTelephone(request.getTelephone());

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getDepartementId() != null) {
            Departement dept = departementRepository.findById(request.getDepartementId())
                    .orElseThrow(() -> new ResourceNotFoundException("Département non trouvé"));
            user.setDepartement(dept);
        }

        return mapToDto(utilisateurRepository.save(user));
    }

    @Transactional
    public void toggleStatut(Long id) {
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé"));
        user.setActif(!user.getActif());
        utilisateurRepository.save(user);
    }

    public UtilisateurDto mapToDto(Utilisateur u) {
        return UtilisateurDto.builder()
                .id(u.getId())
                .matricule(u.getMatricule())
                .nom(u.getNom())
                .prenom(u.getPrenom())
                .email(u.getEmail())
                .role(u.getRole())
                .poste(u.getPoste())
                .telephone(u.getTelephone())
                .departementId(u.getDepartement() != null ? u.getDepartement().getId() : null)
                .departementNom(u.getDepartement() != null ? u.getDepartement().getNom() : "Non affecté")
                .actif(u.getActif())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
