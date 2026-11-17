package tn.gct.conge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.gct.conge.config.JwtUtil;
import tn.gct.conge.dto.AuthResponse;
import tn.gct.conge.dto.LoginRequest;
import tn.gct.conge.dto.RegisterRequest;
import tn.gct.conge.entity.*;
import tn.gct.conge.exception.BusinessException;
import tn.gct.conge.repository.DepartementRepository;
import tn.gct.conge.repository.SoldeCongeRepository;
import tn.gct.conge.repository.TypeCongeRepository;
import tn.gct.conge.repository.UtilisateurRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final DepartementRepository departementRepository;
    private final TypeCongeRepository typeCongeRepository;
    private final SoldeCongeRepository soldeCongeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Utilisateur user = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));

        if (!user.getActif()) {
            throw new BusinessException("Ce compte utilisateur a été désactivé par l'administrateur");
        }

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("id", user.getId());
        extraClaims.put("nomComplet", user.getPrenom() + " " + user.getNom());
        if (user.getDepartement() != null) {
            extraClaims.put("departementId", user.getDepartement().getId());
            extraClaims.put("departementNom", user.getDepartement().getNom());
        }

        String token = jwtUtil.generateToken(user, extraClaims);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .matricule(user.getMatricule())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .role(user.getRole())
                .departementId(user.getDepartement() != null ? user.getDepartement().getId() : null)
                .departementNom(user.getDepartement() != null ? user.getDepartement().getNom() : null)
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Un utilisateur existe déjà avec cet email");
        }
        if (utilisateurRepository.existsByMatricule(request.getMatricule())) {
            throw new BusinessException("Ce matricule est déjà attribué");
        }

        Departement dept = null;
        if (request.getDepartementId() != null) {
            dept = departementRepository.findById(request.getDepartementId())
                    .orElseThrow(() -> new BusinessException("Département non trouvé"));
        }

        Utilisateur nouvelUtilisateur = Utilisateur.builder()
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

        Utilisateur saved = utilisateurRepository.save(nouvelUtilisateur);

        // Initialisation automatique des soldes de l'année en cours
        int annee = LocalDate.now().getYear();
        List<TypeConge> types = typeCongeRepository.findAll();
        for (TypeConge t : types) {
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

        return login(LoginRequest.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .build());
    }
}
