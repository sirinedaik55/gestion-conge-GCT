package tn.gct.conge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartementDto {
    private Long id;

    @NotBlank(message = "Le nom du département est obligatoire")
    private String nom;

    @NotBlank(message = "Le code du département est obligatoire")
    private String code;

    private String description;
    private Integer maxAbsencesSimultanees;
    private Long totalEmployes;
}
