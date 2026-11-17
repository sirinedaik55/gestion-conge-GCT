package tn.gct.conge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.gct.conge.entity.RoleUtilisateur;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String matricule;
    private String nom;
    private String prenom;
    private String email;
    private RoleUtilisateur role;
    private Long departementId;
    private String departementNom;
}
