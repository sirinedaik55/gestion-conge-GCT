import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatDividerModule } from '@angular/material/divider';
import { AuthService } from '../../../core/auth/auth.service';
import { TranslationService } from '../../../core/services/translation.service';
import { NotificationService } from '../../../core/services/notification.service';
import { Notification } from '../../../core/models/notification.model';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule, MatMenuModule, MatBadgeModule, MatDividerModule],
  template: `
    <header class="navbar">
      <div class="brand">
        <div class="logo-box">
          <span class="logo-text">GCT</span>
        </div>
        <div class="brand-titles">
          <h1>{{ t.t('app_title') }}</h1>
          <p>{{ t.t('app_subtitle') }}</p>
        </div>
      </div>

      <div class="nav-actions">
        <!-- Switch langue Arabe / Français -->
        <button class="lang-btn" (click)="t.toggleLanguage()" [title]="t.currentLang() === 'fr' ? 'التحويل إلى العربية' : 'Passer en Français'">
          <span class="lang-flag">{{ t.currentLang() === 'fr' ? '🇹🇳 AR' : '🇫🇷 FR' }}</span>
        </button>

        <!-- Cloche Notifications -->
        <button class="icon-btn" [matMenuTriggerFor]="notifMenu" (click)="loadNotifications()">
          <mat-icon [matBadge]="notifService.unreadCount()" [matBadgeHidden]="notifService.unreadCount() === 0" matBadgeColor="warn">
            notifications
          </mat-icon>
        </button>

        <mat-menu #notifMenu="matMenu" class="notif-menu">
          <div class="notif-header" (click)="$event.stopPropagation()">
            <span>Notifications</span>
            <button class="mark-all-btn" (click)="marquerToutesLues()">Tout marquer lu</button>
          </div>
          <div *ngIf="notifications.length === 0" class="empty-notif" (click)="$event.stopPropagation()">
            Aucune notification récente
          </div>
          <div *ngFor="let n of notifications" class="notif-item" [class.unread]="!n.lue" (click)="lireNotif(n)">
            <p class="notif-title">{{ n.titre }}</p>
            <p class="notif-msg">{{ n.message }}</p>
            <span class="notif-date">{{ n.dateCreation | date:'short' }}</span>
          </div>
        </mat-menu>

        <!-- Profil Utilisateur -->
        <div class="user-pill" [matMenuTriggerFor]="userMenu" *ngIf="auth.currentUser() as user">
          <div class="avatar">{{ user.prenom[0] }}{{ user.nom[0] }}</div>
          <div class="user-info">
            <span class="user-name">{{ user.prenom }} {{ user.nom }}</span>
            <span class="user-role">{{ user.role }}</span>
          </div>
          <mat-icon>expand_more</mat-icon>
        </div>

        <mat-menu #userMenu="matMenu">
          <div class="menu-user-header" *ngIf="auth.currentUser() as u">
            <strong>{{ u.prenom }} {{ u.nom }}</strong>
            <small>{{ u.email }}</small>
            <span class="dept-tag">{{ u.departementNom || 'Groupe Chimique' }}</span>
          </div>
          <mat-divider></mat-divider>
          <button mat-menu-item (click)="auth.logout()">
            <mat-icon>logout</mat-icon>
            <span>{{ t.t('nav_logout') }}</span>
          </button>
        </mat-menu>
      </div>
    </header>
  `,
  styles: [`
    .navbar {
      height: 70px;
      background: white;
      border-bottom: 1px solid var(--gct-border);
      padding: 0 1.5rem;
      display: flex;
      align-items: center;
      justify-content: space-between;
      position: sticky;
      top: 0;
      z-index: 50;
    }
    .brand {
      display: flex;
      align-items: center;
      gap: 1rem;
    }
    .logo-box {
      width: 44px;
      height: 44px;
      border-radius: 10px;
      background: linear-gradient(135deg, var(--gct-navy), var(--gct-blue));
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-weight: 800;
      font-size: 1.1rem;
      letter-spacing: 1px;
    }
    .brand-titles h1 {
      font-size: 1.1rem;
      font-weight: 700;
      color: var(--gct-navy);
      line-height: 1.2;
    }
    .brand-titles p {
      font-size: 0.75rem;
      color: #718096;
    }
    .nav-actions {
      display: flex;
      align-items: center;
      gap: 1rem;
    }
    .lang-btn {
      background: #edf2f7;
      border: 1px solid #cbd5e0;
      padding: 6px 12px;
      border-radius: 20px;
      font-weight: 600;
      font-size: 0.8rem;
      cursor: pointer;
      transition: background 0.2s;
      &:hover { background: #e2e8f0; }
    }
    .icon-btn {
      background: transparent;
      border: none;
      cursor: pointer;
      color: #4a5568;
      display: flex;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      border-radius: 50%;
      &:hover { background: #f7fafc; }
    }
    .user-pill {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 6px 12px;
      background: #f7fafc;
      border-radius: 30px;
      border: 1px solid var(--gct-border);
      cursor: pointer;
      &:hover { background: #edf2f7; }
    }
    .avatar {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: var(--gct-teal);
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 600;
      font-size: 0.85rem;
    }
    .user-info {
      display: flex;
      flex-direction: column;
      text-align: left;
    }
    .user-name { font-size: 0.85rem; font-weight: 600; color: #2d3748; }
    .user-role { font-size: 0.7rem; color: #718096; font-weight: 500; }
    .notif-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px 16px;
      font-weight: 600;
      border-bottom: 1px solid #e2e8f0;
    }
    .mark-all-btn {
      background: none;
      border: none;
      color: var(--gct-blue);
      font-size: 0.75rem;
      cursor: pointer;
    }
    .notif-item {
      padding: 10px 16px;
      max-width: 320px;
      border-bottom: 1px solid #f1f5f9;
      cursor: pointer;
      &.unread { background: #f0fdf4; }
      &:hover { background: #f8fafc; }
    }
    .notif-title { font-size: 0.85rem; font-weight: 600; margin-bottom: 2px; }
    .notif-msg { font-size: 0.78rem; color: #64748b; line-height: 1.3; }
    .notif-date { font-size: 0.7rem; color: #94a3b8; }
    .empty-notif { padding: 20px; text-align: center; color: #94a3b8; font-size: 0.85rem; }
    .menu-user-header {
      padding: 12px 16px;
      display: flex;
      flex-direction: column;
      gap: 2px;
    }
    .dept-tag {
      font-size: 0.75rem;
      background: #e2e8f0;
      padding: 2px 6px;
      border-radius: 4px;
      width: fit-content;
      margin-top: 4px;
    }
  `]
})
export class NavbarComponent implements OnInit {
  notifications: Notification[] = [];

  constructor(
    public auth: AuthService,
    public t: TranslationService,
    public notifService: NotificationService
  ) {}

  ngOnInit() {
    this.notifService.refreshCount();
  }

  loadNotifications() {
    this.notifService.getNotifications().subscribe(data => {
      this.notifications = data.slice(0, 8);
    });
  }

  lireNotif(notif: Notification) {
    if (!notif.lue) {
      this.notifService.marquerCommeLue(notif.id).subscribe(() => {
        notif.lue = true;
      });
    }
  }

  marquerToutesLues() {
    this.notifService.marquerToutesCommeLues().subscribe(() => {
      this.notifications.forEach(n => n.lue = true);
    });
  }
}
