package tn.gct.conge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.gct.conge.entity.Notification;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByDestinataireIdOrderByDateCreationDesc(Long destinataireId);
    List<Notification> findByDestinataireIdAndLueFalseOrderByDateCreationDesc(Long destinataireId);
    long countByDestinataireIdAndLueFalse(Long destinataireId);
}
