export interface Departement {
  id: number;
  nom: string;
  code: string;
  description?: string;
  maxAbsencesSimultanees: number;
  totalEmployes?: number;
}
