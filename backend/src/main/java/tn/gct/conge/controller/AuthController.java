package tn.gct.conge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.gct.conge.dto.AuthResponse;
import tn.gct.conge.dto.LoginRequest;
import tn.gct.conge.dto.RegisterRequest;
import tn.gct.conge.dto.UtilisateurDto;
import tn.gct.conge.entity.Utilisateur;
import tn.gct.conge.service.AuthService;
import tn.gct.conge.service.UtilisateurService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Endpoints de connexion et profil utilisateur GCT")
public class AuthController {

    private final AuthService authService;
    private final UtilisateurService utilisateurService;

    @PostMapping("/login")
    @Operation(summary = "Connexion utilisateur avec email et mot de passe")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    @Operation(summary = "Création d'un nouveau compte employé GCT")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Récupérer les informations de l'utilisateur connecté")
    public ResponseEntity<UtilisateurDto> getCurrentUser(@AuthenticationPrincipal Utilisateur user) {
        return ResponseEntity.ok(utilisateurService.mapToDto(user));
    }
}
