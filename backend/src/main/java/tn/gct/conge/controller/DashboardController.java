package tn.gct.conge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.gct.conge.dto.DashboardStatsDto;
import tn.gct.conge.entity.Utilisateur;
import tn.gct.conge.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Tableau de bord", description = "Statistiques et indicateurs pour employés, chefs et RH")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "Obtenir les statistiques globales et personnelles pour le dashboard")
    public ResponseEntity<DashboardStatsDto> getStats(@AuthenticationPrincipal Utilisateur user) {
        return ResponseEntity.ok(dashboardService.getStats(user));
    }
}
