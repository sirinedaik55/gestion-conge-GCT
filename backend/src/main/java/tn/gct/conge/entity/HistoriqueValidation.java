package tn.gct.conge.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "historique_validation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueValidation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_id", nullable = false)
    private DemandeConge demande;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "valideur_id")
    private Utilisateur valideur;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(name = "statut_precedent", length = 50)
    private String statutPrecedent;

    @Column(name = "nouveau_statut", nullable = false, length = 50)
    private String nouveauStatut;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @CreationTimestamp
    @Column(name = "date_action", updatable = false)
    private LocalDateTime dateAction;
}
