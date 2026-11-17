# 🏭 Application de Gestion des Congés — Groupe Chimique Tunisien (GCT)

> Système d'information complet pour la gestion, la planification et la validation des congés des personnels et directions du **Groupe Chimique Tunisien (GCT)**.

---

## 📌 Architecture & Stack Technique

- **Frontend :** Angular 17 (Standalone Components, Angular Material, RxJS, Chart.js, FullCalendar, Support Bilingue Français 🇫🇷 & Arabe 🇹🇳 avec RTL)
- **Backend :** Spring Boot 3.2 (Java 17, Spring Security 6, JWT, Spring Data JPA, Hibernate, OpenAPI/Swagger 3)
- **Base de données :** PostgreSQL 15 (Migrations automatisées avec Flyway)
- **Conteneurisation :** Docker, Docker Compose
- **DevOps / Intégration Continue :** GitHub Actions (Pipelines CI de tests/build et CD de déploiement d'images de conteneurs)

---

## ⚖️ Règles Métier & Spécificités du GCT

1. **⚡ Congé de Maternité (Acceptation Automatique) :**
   - Dès la soumission d'une demande de congé de maternité (98 jours légaux), la demande passe **immédiatement et automatiquement** au statut `APPROUVE`.
   - **Aucun délai ni validation** n'est exigé de la part du Chef de Département ou des RH.
   - Les notifications et emails de confirmation sont envoyés sur le champ.

2. **👥 Règle de non-chevauchement départemental (Max 2 personnes) :**
   - **Pas plus de deux (2) collaborateurs** du même département ou unité de production ne peuvent être simultanément en congé sur une période commune.
   - Le système vérifie cette règle lors de chaque soumission et bloque tout dépassement en levant une alerte explicite.

3. **🔄 Workflow d'approbation à double niveau :**
   - Pour les congés normaux (annuel, sans solde, etc.) :
     $$\text{Employé (Soumission)} \longrightarrow \text{Chef de Département (Visa favorable)} \longrightarrow \text{Direction RH (Accord final)}$$
   - Le décompte effectif sur le solde de congés n'intervient qu'après l'accord final RH (ou immédiatement si auto-approuvé).

4. **📅 Calcul en jours ouvrés :**
   - Décompte automatique excluant les samedis et dimanches.
   - Contrôle strict de la non-superposition de dates pour un même employé.
   - Vérification préalable du solde de jours restants.

---

## 👥 Comptes de Démonstration Pré-configurés

Tous les comptes sont initialisés par défaut avec le mot de passe : `password123`

| Rôle | Nom & Prénom | Email | Description |
|---|---|---|---|
| **ADMIN** | Mohamed Trabelsi | `admin@gct.tn` | Gestion globale, départements, comptes |
| **RH** | Fatma Ben Salem | `rh@gct.tn` | Validation finale des congés, soldes |
| **CHEF_DEPARTEMENT** | Kamel Gharbi | `chef.mdhilla@gct.tn` | Chef d'unité Usine Mdhilla (Visa des demandes) |
| **EMPLOYE** | Syrine Mansouri | `syrine.employe@gct.tn` | Ingénieure Chimiste (Dépôt demandes) |
| **EMPLOYE** | Ahmed Jaziri | `ahmed.employe@gct.tn` | Technicien même usine (Test règle 2 personnes) |

---

## 🚀 Démarrage Rapide

### Option 1 : Lancement avec Docker Compose (Recommandé)

Assurez-vous que Docker Desktop est démarré, puis lancez :

```bash
docker-compose up --build
```

- **Frontend Angular :** [http://localhost:4200](http://localhost:4200)
- **Backend Spring Boot :** [http://localhost:8080](http://localhost:8080)
- **Documentation Swagger UI :** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Base PostgreSQL :** `localhost:5432` (db: `gct_conge`, user: `gct_user`, pass: `gct_pass`)

---

### Option 2 : Lancement en local (Développement)

#### 1. Démarrer PostgreSQL
Créez la base `gct_conge` sur PostgreSQL local :
```sql
CREATE DATABASE gct_conge;
```

#### 2. Démarrer le Backend (Spring Boot)
```bash
cd backend
mvn spring-boot:run
```

#### 3. Démarrer le Frontend (Angular)
```bash
cd frontend
npm install
npm start
```
Rendez-vous ensuite sur `http://localhost:4200`.

---

## 📂 Structure du Répertoire

```
Gestion_Congé_GCT/
├── backend/                              # Application Spring Boot 3.2
│   ├── src/main/java/tn/gct/conge/
│   │   ├── config/                       # Sécurité JWT, CORS, OpenAPI, Seeding
│   │   ├── controller/                   # Endpoints REST (Auth, Congés, etc.)
│   │   ├── dto/                          # Objets de transfert de données
│   │   ├── entity/                       # Entités JPA (Utilisateur, Demande, etc.)
│   │   ├── exception/                    # Exceptions métiers & Handler global
│   │   ├── repository/                   # Spring Data Repositories
│   │   └── service/                      # Logique métier et règles GCT
│   ├── src/main/resources/
│   │   ├── application.yml               # Paramétrage de l'application
│   │   └── db/migration/V1__init.sql     # Schéma initial Flyway
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                             # Application Angular 17
│   ├── src/app/
│   │   ├── core/                         # Auth, JWT Interceptor, Services, Modèles
│   │   ├── modules/                      # Auth, Dashboard, Congés, Employés, Dépts
│   │   └── shared/                       # Navbar, Sidebar, Badges, Pipes
│   ├── src/assets/i18n/                  # Dictionnaires FR 🇫🇷 et AR 🇹🇳
│   ├── Dockerfile
│   ├── nginx.conf                        # Reverse proxy Nginx
│   └── package.json
├── .github/workflows/
│   ├── ci.yml                            # Pipeline CI (Tests + Packaging)
│   └── cd.yml                            # Pipeline CD (Push image conteneur)
├── docker-compose.yml                    # Orchestration conteneurisée
├── .env.example
├── .gitignore
└── README.md
```

---

## 🔒 Endpoints REST Principaux

| Méthode | URI | Description | Rôles Autorisés |
|---|---|---|---|
| `POST` | `/api/auth/login` | Connexion et obtention du JWT | Public |
| `POST` | `/api/conges` | Soumettre un congé (Auto maternité ou visa) | Tous |
| `GET` | `/api/conges` | Liste filtrée selon le rôle de l'utilisateur | Tous |
| `PUT` | `/api/conges/{id}/valider-chef` | Visa favorable du Chef de département | CHEF, ADMIN |
| `PUT` | `/api/conges/{id}/valider-rh` | Validation définitive RH | RH, ADMIN |
| `PUT` | `/api/conges/{id}/rejeter` | Refus motivé d'une demande | CHEF, RH, ADMIN |
| `PUT` | `/api/conges/{id}/annuler` | Annulation par l'employé | EMPLOYE |
| `GET` | `/api/conges/calendrier` | Flux calendrier des absences validées | Tous |
| `GET` | `/api/soldes/mes-soldes` | Consultation de ses soldes de jours | Tous |
| `GET` | `/api/dashboard/stats` | Données agrégées pour le tableau de bord | Tous |
| `GET` | `/api/utilisateurs` | Gestion des effectifs | RH, ADMIN |
| `GET` | `/api/departements` | Liste des unités & plafonds d'absences | Tous |

---

*Projet réalisé pour le Groupe Chimique Tunisien (GCT).*
