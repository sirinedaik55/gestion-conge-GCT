import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Utilisateur } from '../models/utilisateur.model';

@Injectable({
  providedIn: 'root'
})
export class UtilisateurService {
  private apiUrl = `${environment.apiUrl}/utilisateurs`;

  constructor(private http: HttpClient) {}

  getAll(departementId?: number): Observable<Utilisateur[]> {
    const url = departementId ? `${this.apiUrl}?departementId=${departementId}` : this.apiUrl;
    return this.http.get<Utilisateur[]>(url);
  }

  getById(id: number): Observable<Utilisateur> {
    return this.http.get<Utilisateur>(`${this.apiUrl}/${id}`);
  }

  creer(user: any): Observable<Utilisateur> {
    return this.http.post<Utilisateur>(this.apiUrl, user);
  }

  modifier(id: number, user: any): Observable<Utilisateur> {
    return this.http.put<Utilisateur>(`${this.apiUrl}/${id}`, user);
  }

  toggleStatut(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/toggle-statut`, {});
  }
}
