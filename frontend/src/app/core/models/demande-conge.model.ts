export type StatutDemande = 'EN_ATTENTE' | 'APPROUVE_CHEF' | 'APPROUVE' | 'REJETE' | 'ANNULE';

export interface TypeConge {
  id: number;
  nom: string;
  code: string;
  description?: string;
  joursMaxParAn: number;
  autoApprouve: boolean;
  justificatifRequis: boolean;
  cumulable: boolean;
}

export interface DemandeConge {
  id: number;
  employeId: number;
  employeNom: string;
  employePrenom: string;
  employeMatricule: string;
  departementNom: string;
  typeCongeId: number;
  typeCongeNom: string;
  typeCongeCode: string;
  autoApprouve: boolean;
  dateDebut: string;
  dateFin: string;
  nbJours: number;
  statut: StatutDemande;
  motif?: string;
  justificatifUrl?: string;
  commentaireRefus?: string;
  dateValidationChef?: string;
  chefValideurNom?: string;
  dateValidationRh?: string;
  rhValideurNom?: string;
  dateSoumission: string;
}

export interface DemandeCongeRequest {
  typeCongeId: number;
  dateDebut: string;
  dateFin: string;
  motif?: string;
  justificatifUrl?: string;
}

export interface SoldeConge {
  id: number;
  typeCongeId: number;
  typeCongeNom: string;
  typeCongeCode: string;
  annee: number;
  joursAcquis: number;
  joursPris: number;
  joursRestants: number;
}
