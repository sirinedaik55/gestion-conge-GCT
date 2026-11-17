package tn.gct.conge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.gct.conge.entity.SoldeConge;

import java.util.List;
import java.util.Optional;

@Repository
public interface SoldeCongeRepository extends JpaRepository<SoldeConge, Long> {
    List<SoldeConge> findByEmployeIdAndAnnee(Long employeId, Integer annee);
    List<SoldeConge> findByEmployeId(Long employeId);
    Optional<SoldeConge> findByEmployeIdAndTypeCongeIdAndAnnee(Long employeId, Long typeCongeId, Integer annee);
    boolean existsByEmployeIdAndTypeCongeIdAndAnnee(Long employeId, Long typeCongeId, Integer annee);
}
