package tn.gct.conge.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "demande_conge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeConge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employe_id", nullable = false)
    private Utilisateur employe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_conge_id", nullable = false)
    private TypeConge typeConge;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(name = "nb_jours", nullable = false)
    private Integer nbJours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private StatutDemande statut = StatutDemande.EN_ATTENTE;

    @Column(columnDefinition = "TEXT")
    private String motif;

    @Column(name = "justificatif_url")
    private String justificatifUrl;

    @Column(name = "commentaire_refus", columnDefinition = "TEXT")
    private String commentaireRefus;

    @Column(name = "date_validation_chef")
    private LocalDateTime dateValidationChef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chef_valideur_id")
    private Utilisateur chefValideur;

    @Column(name = "date_validation_rh")
    private LocalDateTime dateValidationRh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rh_valideur_id")
    private Utilisateur rhValideur;

    @CreationTimestamp
    @Column(name = "date_soumission", updatable = false)
    private LocalDateTime dateSoumission;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
