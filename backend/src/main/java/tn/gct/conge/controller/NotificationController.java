package tn.gct.conge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.gct.conge.dto.NotificationDto;
import tn.gct.conge.entity.Utilisateur;
import tn.gct.conge.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Système de notifications in-app pour les agents et valideurs")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Lister toutes les notifications de l'utilisateur connecté")
    public ResponseEntity<List<NotificationDto>> getMesNotifications(@AuthenticationPrincipal Utilisateur user) {
        return ResponseEntity.ok(notificationService.getNotificationsUtilisateur(user.getId()));
    }

    @GetMapping("/non-lues")
    @Operation(summary = "Lister les notifications non lues")
    public ResponseEntity<List<NotificationDto>> getNotificationsNonLues(@AuthenticationPrincipal Utilisateur user) {
        return ResponseEntity.ok(notificationService.getNotificationsNonLues(user.getId()));
    }

    @GetMapping("/count-non-lues")
    @Operation(summary = "Compter les notifications non lues (pour badge)")
    public ResponseEntity<Long> getCountNonLues(@AuthenticationPrincipal Utilisateur user) {
        return ResponseEntity.ok(notificationService.getNombreNotificationsNonLues(user.getId()));
    }

    @PatchMapping("/{id}/marquer-lue")
    @Operation(summary = "Marquer une notification comme lue")
    public ResponseEntity<Void> marquerCommeLue(@PathVariable Long id, @AuthenticationPrincipal Utilisateur user) {
        notificationService.marquerCommeLue(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/marquer-toutes-lues")
    @Operation(summary = "Marquer toutes ses notifications comme lues")
    public ResponseEntity<Void> marquerToutesLues(@AuthenticationPrincipal Utilisateur user) {
        notificationService.marquerToutesCommeLues(user.getId());
        return ResponseEntity.ok().build();
    }
}
