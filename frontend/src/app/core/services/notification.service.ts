import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Notification } from '../models/notification.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = `${environment.apiUrl}/notifications`;
  unreadCount = signal<number>(0);

  constructor(private http: HttpClient) {}

  getNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(this.apiUrl);
  }

  getNonLues(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/non-lues`);
  }

  refreshCount() {
    this.http.get<number>(`${this.apiUrl}/count-non-lues`).subscribe(count => {
      this.unreadCount.set(count);
    });
  }

  marquerCommeLue(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/marquer-lue`, {}).pipe(
      tap(() => this.refreshCount())
    );
  }

  marquerToutesCommeLues(): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/marquer-toutes-lues`, {}).pipe(
      tap(() => this.unreadCount.set(0))
    );
  }
}
