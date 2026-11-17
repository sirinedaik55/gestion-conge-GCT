import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, RoleUtilisateur, Utilisateur } from '../models/utilisateur.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  currentUser = signal<AuthResponse | null>(null);

  constructor(private http: HttpClient, private router: Router) {
    const saved = localStorage.getItem('gct_user');
    if (saved) {
      try {
        this.currentUser.set(JSON.parse(saved));
      } catch (e) {
        this.logout();
      }
    }
  }

  login(credentials: { email: string; password: string }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(res => {
        localStorage.setItem('gct_token', res.token);
        localStorage.setItem('gct_user', JSON.stringify(res));
        this.currentUser.set(res);
      })
    );
  }

  logout() {
    localStorage.removeItem('gct_token');
    localStorage.removeItem('gct_user');
    this.currentUser.set(null);
    this.router.navigate(['/auth/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('gct_token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  hasRole(roles: RoleUtilisateur[]): boolean {
    const user = this.currentUser();
    return user ? roles.includes(user.role) : false;
  }
}
