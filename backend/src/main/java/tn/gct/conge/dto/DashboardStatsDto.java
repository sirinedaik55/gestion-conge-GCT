package tn.gct.conge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private long totalEmployes;
    private long totalDepartements;
    private long demandesEnAttente;
    private long demandesApprouvees;
    private long demandesRejetees;
    private int monSoldeRestant;
    private Map<String, Long> repartitionParType;
    private Map<String, Long> repartitionParDepartement;
}
