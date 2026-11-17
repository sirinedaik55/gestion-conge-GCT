-- Migration Flyway V1: Initialisation de la base de données GCT Congé

CREATE TABLE IF NOT EXISTS departement (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(20) NOT NULL UNIQUE,
    description TEXT,
    max_absences_simultanees INT NOT NULL DEFAULT 2,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS utilisateur (
    id BIGSERIAL PRIMARY KEY,
    matricule VARCHAR(30) NOT NULL UNIQUE,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'EMPLOYE',
    poste VARCHAR(100),
    telephone VARCHAR(30),
    departement_id BIGINT REFERENCES departement(id) ON DELETE SET NULL,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS type_conge (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    jours_max_par_an INT NOT NULL,
    auto_approuve BOOLEAN NOT NULL DEFAULT FALSE,
    justificatif_requis BOOLEAN NOT NULL DEFAULT FALSE,
    cumulable BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS demande_conge (
    id BIGSERIAL PRIMARY KEY,
    employe_id BIGINT NOT NULL REFERENCES utilisateur(id) ON DELETE CASCADE,
    type_conge_id BIGINT NOT NULL REFERENCES type_conge(id) ON DELETE RESTRICT,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    nb_jours INT NOT NULL,
    statut VARCHAR(50) NOT NULL DEFAULT 'EN_ATTENTE',
    motif TEXT,
    justificatif_url VARCHAR(255),
    commentaire_refus TEXT,
    date_validation_chef TIMESTAMP WITHOUT TIME ZONE,
    chef_valideur_id BIGINT REFERENCES utilisateur(id) ON DELETE SET NULL,
    date_validation_rh TIMESTAMP WITHOUT TIME ZONE,
    rh_valideur_id BIGINT REFERENCES utilisateur(id) ON DELETE SET NULL,
    date_soumission TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS solde_conge (
    id BIGSERIAL PRIMARY KEY,
    employe_id BIGINT NOT NULL REFERENCES utilisateur(id) ON DELETE CASCADE,
    type_conge_id BIGINT NOT NULL REFERENCES type_conge(id) ON DELETE CASCADE,
    annee INT NOT NULL,
    jours_acquis INT NOT NULL DEFAULT 30,
    jours_pris INT NOT NULL DEFAULT 0,
    jours_restants INT NOT NULL DEFAULT 30,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_solde_employe_type_annee UNIQUE (employe_id, type_conge_id, annee)
);

CREATE TABLE IF NOT EXISTS historique_validation (
    id BIGSERIAL PRIMARY KEY,
    demande_id BIGINT NOT NULL REFERENCES demande_conge(id) ON DELETE CASCADE,
    valideur_id BIGINT REFERENCES utilisateur(id) ON DELETE SET NULL,
    action VARCHAR(50) NOT NULL,
    statut_precedent VARCHAR(50),
    nouveau_statut VARCHAR(50) NOT NULL,
    commentaire TEXT,
    date_action TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notification (
    id BIGSERIAL PRIMARY KEY,
    destinataire_id BIGINT NOT NULL REFERENCES utilisateur(id) ON DELETE CASCADE,
    titre VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    demande_id BIGINT REFERENCES demande_conge(id) ON DELETE CASCADE,
    lue BOOLEAN NOT NULL DEFAULT FALSE,
    date_creation TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index pour optimiser les requêtes fréquentes
CREATE INDEX IF NOT EXISTS idx_demande_employe ON demande_conge(employe_id);
CREATE INDEX IF NOT EXISTS idx_demande_statut ON demande_conge(statut);
CREATE INDEX IF NOT EXISTS idx_demande_dates ON demande_conge(date_debut, date_fin);
CREATE INDEX IF NOT EXISTS idx_solde_employe ON solde_conge(employe_id, annee);
CREATE INDEX IF NOT EXISTS idx_notif_destinataire ON notification(destinataire_id, lue);

-- Données de référence pour les départements du Groupe Chimique Tunisien
INSERT INTO departement (nom, code, description, max_absences_simultanees) VALUES
('Direction Générale', 'DIR_GEN', 'Direction Générale et Stratégie du GCT', 2),
('Ressources Humaines', 'RH', 'Direction des Ressources Humaines et Gestion Sociale', 2),
('Usine Phosphorique Mdhilla', 'USINE_MDH', 'Unité de production d acide phosphorique - Gafsa', 2),
('Usine d Engrais Gabès', 'USINE_GAB', 'Complexe industriel de transformation et d engrais - Gabès', 2),
('Maintenance et Sécurité', 'MAINT_SEC', 'Direction Technique de Maintenance Industrielle et Sécurité', 2),
('Systèmes d Information & Digitalisation', 'IT', 'Département Informatique et Télécommunications', 2),
('Approvisionnement & Logistique', 'LOGISTIQUE', 'Direction des Achats, Logistique et Transit', 2)
ON CONFLICT (nom) DO NOTHING;

-- Données initiales des types de congé avec la règle spéciale du congé maternité auto-approuvé
INSERT INTO type_conge (nom, code, description, jours_max_par_an, auto_approuve, justificatif_requis, cumulable) VALUES
('Congé Annuel Payé', 'ANNUEL', 'Congé de repos annuel payé réglementaire', 30, FALSE, FALSE, TRUE),
('Congé de Maladie Ordinaire', 'MALADIE', 'Arrêt de travail pour maladie prescrit par médecin', 90, FALSE, TRUE, FALSE),
('Congé de Maternité', 'MATERNITE', 'Congé légal de maternité approuvé automatiquement dès soumission', 98, TRUE, FALSE, FALSE),
('Congé de Paternité', 'PATERNITE', 'Congé spécial à l occasion d une naissance', 7, FALSE, TRUE, FALSE),
('Congé sans Solde', 'SANS_SOLDE', 'Congé pour convenances personnelles sans rémunération', 30, FALSE, FALSE, FALSE),
('Congé Exceptionnel / Familial', 'EXCEPTIONNEL', 'Mariage, décès, évènements familiaux particuliers', 7, FALSE, TRUE, FALSE)
ON CONFLICT (nom) DO NOTHING;
