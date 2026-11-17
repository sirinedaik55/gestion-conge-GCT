package tn.gct.conge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.gct.conge.entity.TypeConge;

import java.util.Optional;

@Repository
public interface TypeCongeRepository extends JpaRepository<TypeConge, Long> {
    Optional<TypeConge> findByCode(String code);
    Optional<TypeConge> findByNom(String nom);
    boolean existsByCode(String code);
}
