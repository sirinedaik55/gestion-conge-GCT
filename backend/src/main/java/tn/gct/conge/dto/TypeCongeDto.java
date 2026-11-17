package tn.gct.conge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypeCongeDto {
    private Long id;
    private String nom;
    private String code;
    private String description;
    private Integer joursMaxParAn;
    private Boolean autoApprouve;
    private Boolean justificatifRequis;
    private Boolean cumulable;
}
