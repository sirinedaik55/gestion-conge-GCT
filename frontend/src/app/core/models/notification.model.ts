export interface Notification {
  id: number;
  titre: string;
  message: string;
  type: string;
  demandeId?: number;
  lue: boolean;
  dateCreation: string;
}
