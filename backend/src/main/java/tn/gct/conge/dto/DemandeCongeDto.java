package tn.gct.conge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.gct.conge.entity.StatutDemande;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandeCongeDto {
    private Long id;
    private Long employeId;
    private String employeNom;
    private String employePrenom;
    private String employeMatricule;
    private String departementNom;

    private Long typeCongeId;
    private String typeCongeNom;
    private String typeCongeCode;
    private Boolean autoApprouve;

    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Integer nbJours;
    private StatutDemande statut;
    private String motif;
    private String justificatifUrl;
    private String commentaireRefus;

    private LocalDateTime dateValidationChef;
    private String chefValideurNom;
    private LocalDateTime dateValidationRh;
    private String rhValideurNom;

    private LocalDateTime dateSoumission;
}
