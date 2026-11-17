package tn.gct.conge.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "type_conge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeConge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nom;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "jours_max_par_an", nullable = false)
    private Integer joursMaxParAn;

    @Column(name = "auto_approuve", nullable = false)
    @Builder.Default
    private Boolean autoApprouve = false;

    @Column(name = "justificatif_requis", nullable = false)
    @Builder.Default
    private Boolean justificatifRequis = false;

    @Column(name = "cumulable", nullable = false)
    @Builder.Default
    private Boolean cumulable = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
