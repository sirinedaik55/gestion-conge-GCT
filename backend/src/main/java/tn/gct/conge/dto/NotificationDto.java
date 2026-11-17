package tn.gct.conge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private String titre;
    private String message;
    private String type;
    private Long demandeId;
    private Boolean lue;
    private LocalDateTime dateCreation;
}
