import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../core/auth/auth.service';
import { TranslationService } from '../../../core/services/translation.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule],
  template: `
    <aside class="sidebar">
      <div class="user-badge" *ngIf="auth.currentUser() as user">
        <span class="user-role-badge" [ngClass]="user.role.toLowerCase()">{{ user.role }}</span>
        <span class="dept-name">{{ user.departementNom || 'Groupe Chimique' }}</span>
      </div>

      <nav class="nav-links">
        <a routerLink="/dashboard" routerLinkActive="active" class="nav-item">
          <mat-icon>dashboard</mat-icon>
          <span>{{ t.t('nav_dashboard') }}</span>
        </a>

        <a routerLink="/conges/demande" routerLinkActive="active" class="nav-item highlight">
          <mat-icon>add_circle</mat-icon>
          <span>{{ t.t('nav_demander') }}</span>
        </a>

        <a routerLink="/conges" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}" class="nav-item">
          <mat-icon>event_note</mat-icon>
          <span>{{ t.t('nav_conges') }}</span>
        </a>

        <a routerLink="/conges/calendrier" routerLinkActive="active" class="nav-item">
          <mat-icon>calendar_month</mat-icon>
          <span>{{ t.t('nav_calendrier') }}</span>
        </a>

        <!-- Menu pour Chef de département, RH et Admin -->
        <div class="nav-section-title" *ngIf="auth.hasRole(['CHEF_DEPARTEMENT', 'RH', 'ADMIN'])">
          <span>VALIDATION & GESTION</span>
        </div>

        <a routerLink="/conges/validations" routerLinkActive="active" class="nav-item" *ngIf="auth.hasRole(['CHEF_DEPARTEMENT', 'RH', 'ADMIN'])">
          <mat-icon>fact_check</mat-icon>
          <span>{{ t.t('nav_validations') }}</span>
        </a>

        <a routerLink="/employes" routerLinkActive="active" class="nav-item" *ngIf="auth.hasRole(['RH', 'ADMIN'])">
          <mat-icon>people</mat-icon>
          <span>{{ t.t('nav_employes') }}</span>
        </a>

        <a routerLink="/departements" routerLinkActive="active" class="nav-item" *ngIf="auth.hasRole(['ADMIN', 'RH'])">
          <mat-icon>apartment</mat-icon>
          <span>{{ t.t('nav_departements') }}</span>
        </a>
      </nav>

      <!-- Encadré des règles GCT -->
      <div class="rules-card">
        <div class="rules-header">
          <mat-icon>info</mat-icon>
          <strong>Règles GCT</strong>
        </div>
        <ul>
          <li><strong>Maternité :</strong> {{ t.t('rule_maternity') }}</li>
          <li><strong>Quotas :</strong> {{ t.t('rule_dept_limit') }}</li>
        </ul>
      </div>
    </aside>
  `,
  styles: [`
    .sidebar {
      width: 260px;
      background: white;
      border-right: 1px solid var(--gct-border);
      height: calc(100vh - 70px);
      position: sticky;
      top: 70px;
      display: flex;
      flex-direction: column;
      padding: 1.5rem 1rem;
      overflow-y: auto;
    }
    .user-badge {
      padding: 0.75rem;
      background: #f8fafc;
      border-radius: 8px;
      margin-bottom: 1.5rem;
      border: 1px solid var(--gct-border);
      display: flex;
      flex-direction: column;
      gap: 4px;
    }
    .user-role-badge {
      font-size: 0.75rem;
      font-weight: 700;
      padding: 2px 8px;
      border-radius: 4px;
      width: fit-content;
      &.admin { background: #fee2e2; color: #991b1b; }
      &.rh { background: #fef3c7; color: #92400e; }
      &.chef_departement { background: #e0f2fe; color: #0369a1; }
      &.employe { background: #dcfce7; color: #166534; }
    }
    .dept-name { font-size: 0.8rem; color: #64748b; font-weight: 500; }
    .nav-links {
      display: flex;
      flex-direction: column;
      gap: 0.35rem;
      flex: 1;
    }
    .nav-section-title {
      font-size: 0.68rem;
      font-weight: 700;
      color: #94a3b8;
      letter-spacing: 0.5px;
      margin: 1.2rem 0 0.4rem 0.5rem;
    }
    .nav-item {
      display: flex;
      align-items: center;
      gap: 0.85rem;
      padding: 0.7rem 1rem;
      color: #4a5568;
      text-decoration: none;
      border-radius: 8px;
      font-weight: 500;
      font-size: 0.9rem;
      transition: all 0.2s;
      mat-icon { font-size: 20px; width: 20px; height: 20px; }
      &:hover {
        background: #f1f5f9;
        color: var(--gct-navy);
      }
      &.active {
        background: var(--gct-navy);
        color: white;
      }
      &.highlight {
        background: #eff6ff;
        color: var(--gct-blue);
        font-weight: 600;
        &:hover { background: #dbeafe; }
        &.active { background: var(--gct-navy); color: white; }
      }
    }
    .rules-card {
      margin-top: auto;
      background: #f0fdf4;
      border: 1px solid #bbf7d0;
      border-radius: 8px;
      padding: 0.85rem;
      font-size: 0.75rem;
      color: #166534;
    }
    .rules-header {
      display: flex;
      align-items: center;
      gap: 4px;
      margin-bottom: 6px;
      mat-icon { font-size: 16px; width: 16px; height: 16px; color: #16a34a; }
    }
    .rules-card ul {
      padding-left: 1.2rem;
      line-height: 1.4;
    }
  `]
})
export class SidebarComponent {
  constructor(public auth: AuthService, public t: TranslationService) {}
}
