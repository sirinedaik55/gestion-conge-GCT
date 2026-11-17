package tn.gct.conge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.gct.conge.dto.NotificationDto;
import tn.gct.conge.entity.DemandeConge;
import tn.gct.conge.entity.Notification;
import tn.gct.conge.entity.Utilisateur;
import tn.gct.conge.exception.ResourceNotFoundException;
import tn.gct.conge.repository.NotificationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void envoyerNotification(Utilisateur destinataire, String titre, String message, String type, DemandeConge demande) {
        Notification notification = Notification.builder()
                .destinataire(destinataire)
                .titre(titre)
                .message(message)
                .type(type)
                .demande(demande)
                .lue(false)
                .build();
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getNotificationsUtilisateur(Long utilisateurId) {
        return notificationRepository.findByDestinataireIdOrderByDateCreationDesc(utilisateurId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getNotificationsNonLues(Long utilisateurId) {
        return notificationRepository.findByDestinataireIdAndLueFalseOrderByDateCreationDesc(utilisateurId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getNombreNotificationsNonLues(Long utilisateurId) {
        return notificationRepository.countByDestinataireIdAndLueFalse(utilisateurId);
    }

    @Transactional
    public void marquerCommeLue(Long notificationId, Long utilisateurId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification non trouvée"));

        if (notification.getDestinataire().getId().equals(utilisateurId)) {
            notification.setLue(true);
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void marquerToutesCommeLues(Long utilisateurId) {
        List<Notification> nonLues = notificationRepository.findByDestinataireIdAndLueFalseOrderByDateCreationDesc(utilisateurId);
        nonLues.forEach(n -> n.setLue(true));
        notificationRepository.saveAll(nonLues);
    }

    private NotificationDto mapToDto(Notification notif) {
        return NotificationDto.builder()
                .id(notif.getId())
                .titre(notif.getTitre())
                .message(notif.getMessage())
                .type(notif.getType())
                .demandeId(notif.getDemande() != null ? notif.getDemande().getId() : null)
                .lue(notif.getLue())
                .dateCreation(notif.getDateCreation())
                .build();
    }
}
