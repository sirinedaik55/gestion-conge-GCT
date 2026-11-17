package tn.gct.conge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.gct.conge.dto.DepartementDto;
import tn.gct.conge.service.DepartementService;

import java.util.List;

@RestController
@RequestMapping("/api/departements")
@RequiredArgsConstructor
@Tag(name = "Départements", description = "Gestion des départements et usines du Groupe Chimique Tunisien")
public class DepartementController {

    private final DepartementService departementService;

    @GetMapping
    @Operation(summary = "Lister tous les départements du GCT")
    public ResponseEntity<List<DepartementDto>> getAllDepartements() {
        return ResponseEntity.ok(departementService.getAllDepartements());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir les détails d'un département")
    public ResponseEntity<DepartementDto> getDepartementById(@PathVariable Long id) {
        return ResponseEntity.ok(departementService.getDepartementById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un nouveau département (Admin)")
    public ResponseEntity<DepartementDto> creerDepartement(@Valid @RequestBody DepartementDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departementService.creerDepartement(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Modifier un département et ses règles de quotas (Admin)")
    public ResponseEntity<DepartementDto> modifierDepartement(@PathVariable Long id, @Valid @RequestBody DepartementDto dto) {
        return ResponseEntity.ok(departementService.modifierDepartement(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un département (Admin)")
    public ResponseEntity<Void> supprimerDepartement(@PathVariable Long id) {
        departementService.supprimerDepartement(id);
        return ResponseEntity.noContent().build();
    }
}
