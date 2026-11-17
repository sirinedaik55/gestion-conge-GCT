import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Departement } from '../models/departement.model';

@Injectable({
  providedIn: 'root'
})
export class DepartementService {
  private apiUrl = `${environment.apiUrl}/departements`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Departement[]> {
    return this.http.get<Departement[]>(this.apiUrl);
  }

  getById(id: number): Observable<Departement> {
    return this.http.get<Departement>(`${this.apiUrl}/${id}`);
  }

  creer(dept: Partial<Departement>): Observable<Departement> {
    return this.http.post<Departement>(this.apiUrl, dept);
  }

  modifier(id: number, dept: Partial<Departement>): Observable<Departement> {
    return this.http.put<Departement>(`${this.apiUrl}/${id}`, dept);
  }

  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
