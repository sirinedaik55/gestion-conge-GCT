package tn.gct.conge.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tn.gct.conge.entity.*;
import tn.gct.conge.repository.DepartementRepository;
import tn.gct.conge.repository.SoldeCongeRepository;
import tn.gct.conge.repository.TypeCongeRepository;
import tn.gct.conge.repository.UtilisateurRepository;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final DepartementRepository departementRepository;
    private final TypeCongeRepository typeCongeRepository;
    private final SoldeCongeRepository soldeCongeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (utilisateurRepository.count() == 0) {
            log.info("Initialisation des comptes par défaut pour le Groupe Chimique Tunisien...");

            Departement itDept = departementRepository.findByCode("IT").orElse(null);
            Departement rhDept = departementRepository.findByCode("RH").orElse(null);
            Departement usineMdh = departementRepository.findByCode("USINE_MDH").orElse(null);

            String encodedPassword = passwordEncoder.encode("password123");

            // 1. Administrateur
            Utilisateur admin = Utilisateur.builder()
                    .matricule("GCT-ADM-001")
                    .nom("Trabelsi")
                    .prenom("Mohamed")
                    .email("admin@gct.tn")
                    .password(encodedPassword)
                    .role(RoleUtilisateur.ADMIN)
                    .poste("Administrateur Systèmes GCT")
                    .telephone("+216 71 000 001")
                    .departement(itDept)
                    .actif(true)
                    .build();
            utilisateurRepository.save(admin);

            // 2. Responsable RH
            Utilisateur rh = Utilisateur.builder()
                    .matricule("GCT-RH-002")
                    .nom("Ben Salem")
                    .prenom("Fatma")
                    .email("rh@gct.tn")
                    .password(encodedPassword)
                    .role(RoleUtilisateur.RH)
                    .poste("Directrice Ressources Humaines")
                    .telephone("+216 71 000 002")
                    .departement(rhDept)
                    .actif(true)
                    .build();
            utilisateurRepository.save(rh);

            // 3. Chef de département (Usine Mdhilla)
            Utilisateur chef = Utilisateur.builder()
                    .matricule("GCT-CHF-003")
                    .nom("Gharbi")
                    .prenom("Kamel")
                    .email("chef.mdhilla@gct.tn")
                    .password(encodedPassword)
                    .role(RoleUtilisateur.CHEF_DEPARTEMENT)
                    .poste("Chef d'Unité Production Mdhilla")
                    .telephone("+216 76 000 003")
                    .departement(usineMdh)
                    .actif(true)
                    .build();
            utilisateurRepository.save(chef);

            // 4. Employée dans l'usine
            Utilisateur employe1 = Utilisateur.builder()
                    .matricule("GCT-EMP-004")
                    .nom("Mansouri")
                    .prenom("Syrine")
                    .email("syrine.employe@gct.tn")
                    .password(encodedPassword)
                    .role(RoleUtilisateur.EMPLOYE)
                    .poste("Ingénieure Chimiste")
                    .telephone("+216 76 000 004")
                    .departement(usineMdh)
                    .actif(true)
                    .build();
            utilisateurRepository.save(employe1);

            // 5. Autre employé dans la même usine pour tester la règle des 2 personnes max
            Utilisateur employe2 = Utilisateur.builder()
                    .matricule("GCT-EMP-005")
                    .nom("Jaziri")
                    .prenom("Ahmed")
                    .email("ahmed.employe@gct.tn")
                    .password(encodedPassword)
                    .role(RoleUtilisateur.EMPLOYE)
                    .poste("Technicien de Maintenance")
                    .telephone("+216 76 000 005")
                    .departement(usineMdh)
                    .actif(true)
                    .build();
            utilisateurRepository.save(employe2);

            // Initialiser les soldes pour l'année courante
            int anneeCourante = LocalDate.now().getYear();
            List<TypeConge> types = typeCongeRepository.findAll();
            List<Utilisateur> users = List.of(admin, rh, chef, employe1, employe2);

            for (Utilisateur u : users) {
                for (TypeConge t : types) {
                    SoldeConge solde = SoldeConge.builder()
                            .employe(u)
                            .typeConge(t)
                            .annee(anneeCourante)
                            .joursAcquis(t.getJoursMaxParAn())
                            .joursPris(0)
                            .joursRestants(t.getJoursMaxParAn())
                            .build();
                    soldeCongeRepository.save(solde);
                }
            }

            log.info("Comptes et soldes GCT initialisés avec succès ! Mot de passe par défaut : password123");
        }
    }
}
