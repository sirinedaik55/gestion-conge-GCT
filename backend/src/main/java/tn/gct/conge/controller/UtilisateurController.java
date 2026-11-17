package tn.gct.conge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.gct.conge.dto.RegisterRequest;
import tn.gct.conge.dto.UtilisateurDto;
import tn.gct.conge.service.UtilisateurService;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@Tag(name = "Employés", description = "Gestion des employés et personnels GCT")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RH', 'CHEF_DEPARTEMENT')")
    @Operation(summary = "Lister les employés")
    public ResponseEntity<List<UtilisateurDto>> getAllUtilisateurs(@RequestParam(required = false) Long departementId) {
        if (departementId != null) {
            return ResponseEntity.ok(utilisateurService.getUtilisateursByDepartement(departementId));
        }
        return ResponseEntity.ok(utilisateurService.getAllUtilisateurs());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH', 'CHEF_DEPARTEMENT')")
    @Operation(summary = "Obtenir un employé par son ID")
    public ResponseEntity<UtilisateurDto> getUtilisateurById(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.getUtilisateurById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RH')")
    @Operation(summary = "Créer un nouvel employé (Admin ou RH)")
    public ResponseEntity<UtilisateurDto> creerUtilisateur(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurService.creerUtilisateur(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH')")
    @Operation(summary = "Modifier les informations d'un employé")
    public ResponseEntity<UtilisateurDto> modifierUtilisateur(@PathVariable Long id, @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(utilisateurService.modifierUtilisateur(id, request));
    }

    @PatchMapping("/{id}/toggle-statut")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activer ou désactiver un compte employé")
    public ResponseEntity<Void> toggleStatut(@PathVariable Long id) {
        utilisateurService.toggleStatut(id);
        return ResponseEntity.ok().build();
    }
}
