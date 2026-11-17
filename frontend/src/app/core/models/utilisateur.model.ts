export type RoleUtilisateur = 'EMPLOYE' | 'CHEF_DEPARTEMENT' | 'RH' | 'ADMIN';

export interface Utilisateur {
  id: number;
  matricule: string;
  nom: string;
  prenom: string;
  email: string;
  role: RoleUtilisateur;
  poste?: string;
  telephone?: string;
  departementId?: number;
  departementNom?: string;
  actif: boolean;
  createdAt?: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  id: number;
  matricule: string;
  nom: string;
  prenom: string;
  email: string;
  role: RoleUtilisateur;
  departementId?: number;
  departementNom?: string;
}
