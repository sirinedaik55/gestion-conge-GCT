package tn.gct.conge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.gct.conge.entity.RoleUtilisateur;
import tn.gct.conge.entity.Utilisateur;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
    Optional<Utilisateur> findByMatricule(String matricule);
    boolean existsByEmail(String email);
    boolean existsByMatricule(String matricule);
    List<Utilisateur> findByDepartementId(Long departementId);
    List<Utilisateur> findByRole(RoleUtilisateur role);
    List<Utilisateur> findByDepartementIdAndRole(Long departementId, RoleUtilisateur role);
    long countByActifTrue();
}
