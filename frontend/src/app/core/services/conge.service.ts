import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DemandeConge, DemandeCongeRequest, SoldeConge, TypeConge } from '../models/demande-conge.model';

@Injectable({
  providedIn: 'root'
})
export class CongeService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getDemandes(): Observable<DemandeConge[]> {
    return this.http.get<DemandeConge[]>(`${this.apiUrl}/conges`);
  }

  getDemandeById(id: number): Observable<DemandeConge> {
    return this.http.get<DemandeConge>(`${this.apiUrl}/conges/${id}`);
  }

  soumettreDemande(demande: DemandeCongeRequest): Observable<DemandeConge> {
    return this.http.post<DemandeConge>(`${this.apiUrl}/conges`, demande);
  }

  validerParChef(id: number, commentaire?: string): Observable<DemandeConge> {
    return this.http.put<DemandeConge>(`${this.apiUrl}/conges/${id}/valider-chef`, { commentaire });
  }

  validerParRh(id: number, commentaire?: string): Observable<DemandeConge> {
    return this.http.put<DemandeConge>(`${this.apiUrl}/conges/${id}/valider-rh`, { commentaire });
  }

  rejeter(id: number, commentaire: string): Observable<DemandeConge> {
    return this.http.put<DemandeConge>(`${this.apiUrl}/conges/${id}/rejeter`, { commentaire });
  }

  annuler(id: number): Observable<DemandeConge> {
    return this.http.put<DemandeConge>(`${this.apiUrl}/conges/${id}/annuler`, {});
  }

  getCalendrier(debut: string, fin: string): Observable<DemandeConge[]> {
    return this.http.get<DemandeConge[]>(`${this.apiUrl}/conges/calendrier?debut=${debut}&fin=${fin}`);
  }

  getTypesConge(): Observable<TypeConge[]> {
    return this.http.get<TypeConge[]>(`${this.apiUrl}/types-conge`);
  }

  getMesSoldes(): Observable<SoldeConge[]> {
    return this.http.get<SoldeConge[]>(`${this.apiUrl}/soldes/mes-soldes`);
  }

  getDashboardStats(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/dashboard/stats`);
  }
}
