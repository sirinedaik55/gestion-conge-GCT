package tn.gct.conge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.gct.conge.entity.DemandeConge;
import tn.gct.conge.entity.StatutDemande;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DemandeCongeRepository extends JpaRepository<DemandeConge, Long> {

    List<DemandeConge> findByEmployeIdOrderByDateSoumissionDesc(Long employeId);

    List<DemandeConge> findByEmployeDepartementIdOrderByDateSoumissionDesc(Long departementId);

    List<DemandeConge> findByStatutOrderByDateSoumissionDesc(StatutDemande statut);

    List<DemandeConge> findAllByOrderByDateSoumissionDesc();

    // Vérifie si l'employé a déjà une demande active chevauchant ces dates
    @Query("""
        SELECT COUNT(d) > 0 FROM DemandeConge d
        WHERE d.employe.id = :employeId
        AND d.statut NOT IN ('REJETE', 'ANNULE')
        AND (d.dateDebut <= :dateFin AND d.dateFin >= :dateDebut)
    """)
    boolean hasOverlappingDemandeForEmploye(
            @Param("employeId") Long employeId,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin
    );

    // Compte les personnes du même département en congé approuvé ou en cours pendant cette période
    @Query("""
        SELECT COUNT(d) FROM DemandeConge d
        WHERE d.employe.departement.id = :departementId
        AND d.employe.id <> :excludeEmployeId
        AND d.statut IN ('APPROUVE', 'APPROUVE_CHEF')
        AND (d.dateDebut <= :dateFin AND d.dateFin >= :dateDebut)
    """)
    long countConcurrentAbsencesInDepartement(
            @Param("departementId") Long departementId,
            @Param("excludeEmployeId") Long excludeEmployeId,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin
    );

    // Requête pour calendrier général des congés approuvés
    @Query("""
        SELECT d FROM DemandeConge d
        WHERE d.statut = 'APPROUVE'
        AND d.dateDebut <= :finPeriode AND d.dateFin >= :debutPeriode
        ORDER BY d.dateDebut ASC
    """)
    List<DemandeConge> findApprovedForCalendar(
            @Param("debutPeriode") LocalDate debutPeriode,
            @Param("finPeriode") LocalDate finPeriode
    );

    long countByStatut(StatutDemande statut);
}
