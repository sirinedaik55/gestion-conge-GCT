package tn.gct.conge.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "solde_conge", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"employe_id", "type_conge_id", "annee"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoldeConge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employe_id", nullable = false)
    private Utilisateur employe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_conge_id", nullable = false)
    private TypeConge typeConge;

    @Column(nullable = false)
    private Integer annee;

    @Column(name = "jours_acquis", nullable = false)
    @Builder.Default
    private Integer joursAcquis = 30;

    @Column(name = "jours_pris", nullable = false)
    @Builder.Default
    private Integer joursPris = 0;

    @Column(name = "jours_restants", nullable = false)
    @Builder.Default
    private Integer joursRestants = 30;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void recalculerRestants() {
        this.joursRestants = Math.max(0, this.joursAcquis - this.joursPris);
    }
}
