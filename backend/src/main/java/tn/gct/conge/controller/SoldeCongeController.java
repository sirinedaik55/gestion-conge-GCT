package tn.gct.conge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.gct.conge.dto.SoldeCongeDto;
import tn.gct.conge.entity.Utilisateur;
import tn.gct.conge.service.SoldeCongeService;

import java.util.List;

@RestController
@RequestMapping("/api/soldes")
@RequiredArgsConstructor
@Tag(name = "Soldes de Congé", description = "Consultation et gestion des droits et soldes de congés")
public class SoldeCongeController {

    private final SoldeCongeService soldeCongeService;

    @GetMapping("/mes-soldes")
    @Operation(summary = "Consulter ses propres soldes de congé pour l'année courante")
    public ResponseEntity<List<SoldeCongeDto>> getMesSoldes(
            @AuthenticationPrincipal Utilisateur user,
            @RequestParam(required = false) Integer annee
    ) {
        return ResponseEntity.ok(soldeCongeService.getSoldesEmploye(user.getId(), annee));
    }

    @GetMapping("/employe/{employeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH', 'CHEF_DEPARTEMENT')")
    @Operation(summary = "Consulter le solde d'un employé spécifique")
    public ResponseEntity<List<SoldeCongeDto>> getSoldesEmploye(
            @PathVariable Long employeId,
            @RequestParam(required = false) Integer annee
    ) {
        return ResponseEntity.ok(soldeCongeService.getSoldesEmploye(employeId, annee));
    }

    @PostMapping("/initialiser-annee")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Initialiser ou renouveler les soldes annuels pour tous les employés")
    public ResponseEntity<String> initialiserAnnee(@RequestParam Integer annee) {
        soldeCongeService.initialiserSoldesAnnuels(annee);
        return ResponseEntity.ok("Soldes de l'année " + annee + " initialisés avec succès.");
    }
}
