package tn.gct.conge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoldeCongeDto {
    private Long id;
    private Long typeCongeId;
    private String typeCongeNom;
    private String typeCongeCode;
    private Integer annee;
    private Integer joursAcquis;
    private Integer joursPris;
    private Integer joursRestants;
}
