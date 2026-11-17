package tn.gct.conge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.gct.conge.entity.RoleUtilisateur;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurDto {
    private Long id;
    private String matricule;
    private String nom;
    private String prenom;
    private String email;
    private RoleUtilisateur role;
    private String poste;
    private String telephone;
    private Long departementId;
    private String departementNom;
    private Boolean actif;
    private LocalDateTime createdAt;
}
