package tn.gct.conge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.gct.conge.dto.DemandeCongeDto;
import tn.gct.conge.dto.DemandeCongeRequest;
import tn.gct.conge.dto.ValidationActionRequest;
import tn.gct.conge.entity.Utilisateur;
import tn.gct.conge.service.CongeService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/conges")
@RequiredArgsConstructor
@Tag(name = "Demandes de Congé", description = "Gestion du cycle de vie des congés (soumission, double validation Chef -> RH, auto-approbation maternité)")
public class DemandeCongeController {

    private final CongeService congeService;

    @PostMapping
    @Operation(summary = "Soumettre une nouvelle demande de congé (auto-acceptation si maternité, sinon attente validation)")
    public ResponseEntity<DemandeCongeDto> soumettreDemande(
            @AuthenticationPrincipal Utilisateur user,
            @Valid @RequestBody DemandeCongeRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(congeService.soumettreDemande(user.getId(), request));
    }

    @GetMapping
    @Operation(summary = "Lister les demandes de congé selon le rôle de l'utilisateur connecté")
    public ResponseEntity<List<DemandeCongeDto>> getMesDemandes(@AuthenticationPrincipal Utilisateur user) {
        return ResponseEntity.ok(congeService.getDemandesParRole(user));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter le détail d'une demande de congé")
    public ResponseEntity<DemandeCongeDto> getDemandeById(@PathVariable Long id) {
        return ResponseEntity.ok(congeService.getDemandeById(id));
    }

    @PutMapping("/{id}/valider-chef")
    @PreAuthorize("hasAnyRole('CHEF_DEPARTEMENT', 'ADMIN')")
    @Operation(summary = "Validation / Avis favorable par le Chef de Département")
    public ResponseEntity<DemandeCongeDto> validerParChef(
            @PathVariable Long id,
            @AuthenticationPrincipal Utilisateur chef,
            @RequestBody(required = false) ValidationActionRequest request
    ) {
        return ResponseEntity.ok(congeService.validerParChef(id, chef.getId(), request));
    }

    @PutMapping("/{id}/valider-rh")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    @Operation(summary = "Validation finale par les Ressources Humaines (RH)")
    public ResponseEntity<DemandeCongeDto> validerParRh(
            @PathVariable Long id,
            @AuthenticationPrincipal Utilisateur rh,
            @RequestBody(required = false) ValidationActionRequest request
    ) {
        return ResponseEntity.ok(congeService.validerParRh(id, rh.getId(), request));
    }

    @PutMapping("/{id}/rejeter")
    @PreAuthorize("hasAnyRole('CHEF_DEPARTEMENT', 'RH', 'ADMIN')")
    @Operation(summary = "Rejeter une demande avec motif obligatoire")
    public ResponseEntity<DemandeCongeDto> rejeter(
            @PathVariable Long id,
            @AuthenticationPrincipal Utilisateur valideur,
            @RequestBody(required = false) ValidationActionRequest request
    ) {
        return ResponseEntity.ok(congeService.rejeterDemande(id, valideur.getId(), request));
    }

    @PutMapping("/{id}/annuler")
    @Operation(summary = "Annuler une demande de congé par l'employé")
    public ResponseEntity<DemandeCongeDto> annuler(
            @PathVariable Long id,
            @AuthenticationPrincipal Utilisateur user
    ) {
        return ResponseEntity.ok(congeService.annulerDemande(id, user.getId()));
    }

    @GetMapping("/calendrier")
    @Operation(summary = "Récupérer les congés approuvés pour le planning / calendrier")
    public ResponseEntity<List<DemandeCongeDto>> getCalendrier(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    ) {
        return ResponseEntity.ok(congeService.getCalendrierConges(debut, fin));
    }
}
