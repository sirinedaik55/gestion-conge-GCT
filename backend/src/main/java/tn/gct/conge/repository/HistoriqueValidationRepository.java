package tn.gct.conge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.gct.conge.entity.HistoriqueValidation;

import java.util.List;

@Repository
public interface HistoriqueValidationRepository extends JpaRepository<HistoriqueValidation, Long> {
    List<HistoriqueValidation> findByDemandeIdOrderByDateActionAsc(Long demandeId);
}
