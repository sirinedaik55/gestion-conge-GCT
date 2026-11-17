import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { CongeService } from '../../core/services/conge.service';
import { AuthService } from '../../core/auth/auth.service';
import { TranslationService } from '../../core/services/translation.service';
import { SoldeConge } from '../../core/models/demande-conge.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule],
  template: `
    <div class="dashboard-page">
      <div class="welcome-banner">
        <div>
          <h2>Bienvenue, {{ auth.currentUser()?.prenom }} {{ auth.currentUser()?.nom }}</h2>
          <p>{{ auth.currentUser()?.role }} — {{ auth.currentUser()?.departementNom || 'Groupe Chimique Tunisien' }}</p>
        </div>
        <a routerLink="/conges/demande" class="btn-demander">
          <mat-icon>add</mat-icon>
          <span>Déposer une demande</span>
        </a>
      </div>

      <!-- Cartes statistiques -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon icon-solde">
            <mat-icon>beach_access</mat-icon>
          </div>
          <div class="stat-content">
            <span class="stat-label">Mon Solde Annuel</span>
            <span class="stat-val">{{ stats?.monSoldeRestant ?? 30 }} <small>jours</small></span>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon icon-pending">
            <mat-icon>hourglass_top</mat-icon>
          </div>
          <div class="stat-content">
            <span class="stat-label">Demandes en attente</span>
            <span class="stat-val">{{ stats?.demandesEnAttente ?? 0 }}</span>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon icon-approved">
            <mat-icon>verified</mat-icon>
          </div>
          <div class="stat-content">
            <span class="stat-label">Congés validés</span>
            <span class="stat-val">{{ stats?.demandesApprouvees ?? 0 }}</span>
          </div>
        </div>

        <div class="stat-card" *ngIf="auth.hasRole(['ADMIN', 'RH'])">
          <div class="stat-icon icon-team">
            <mat-icon>badge</mat-icon>
          </div>
          <div class="stat-content">
            <span class="stat-label">Total Effectif GCT</span>
            <span class="stat-val">{{ stats?.totalEmployes ?? 0 }}</span>
          </div>
        </div>
      </div>

      <!-- Soldes détaillés de l'employé -->
      <div class="section-card">
        <h3>Mes Droits & Soldes de Congé (Année en cours)</h3>
        <div class="soldes-grid">
          <div *ngFor="let s of soldes" class="solde-item">
            <div class="solde-header">
              <span class="solde-title">{{ s.typeCongeNom }}</span>
              <span class="solde-badge" [class.auto]="s.typeCongeCode === 'MATERNITE'">
                {{ s.typeCongeCode === 'MATERNITE' ? 'Accepté Auto' : 'Sur Visa' }}
              </span>
            </div>
            <div class="solde-progress">
              <div class="bar">
                <div class="fill" [style.width.%]="(s.joursPris / s.joursAcquis) * 100"></div>
              </div>
              <div class="solde-counts">
                <span>Pris: {{ s.joursPris }}j</span>
                <strong>Restant: {{ s.joursRestants }}j / {{ s.joursAcquis }}j</strong>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Rappel des règles d'entreprise GCT -->
      <div class="rules-banner">
        <div class="rule-icon"><mat-icon>verified_user</mat-icon></div>
        <div class="rule-text">
          <h4>Règles de fonctionnement au sein du Groupe Chimique Tunisien</h4>
          <p><strong>1. Congé Maternité :</strong> Dépôt avec acceptation automatique et immédiate dès soumission, sans attente de visa du Chef ou des RH.</p>
          <p><strong>2. Règle de non-chevauchement départemental :</strong> Au maximum 2 personnes du même département peuvent être en congé simultanément afin de garantir la continuité de l'activité industrielle.</p>
          <p><strong>3. Circuit de validation :</strong> Avis favorable du Chef d'Unité/Département puis validation définitive de la Direction RH.</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-page {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }
    .welcome-banner {
      background: white;
      border: 1px solid var(--gct-border);
      border-radius: 12px;
      padding: 1.5rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 1rem;
    }
    .welcome-banner h2 {
      font-size: 1.4rem;
      color: var(--gct-navy);
      margin-bottom: 4px;
    }
    .welcome-banner p { color: #64748b; font-size: 0.9rem; }
    .btn-demander {
      background: var(--gct-blue);
      color: white;
      text-decoration: none;
      padding: 0.75rem 1.25rem;
      border-radius: 8px;
      display: flex;
      align-items: center;
      gap: 6px;
      font-weight: 600;
      transition: background 0.2s;
      &:hover { background: var(--gct-navy); }
    }
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 1.25rem;
    }
    .stat-card {
      background: white;
      border: 1px solid var(--gct-border);
      border-radius: 12px;
      padding: 1.25rem;
      display: flex;
      align-items: center;
      gap: 1rem;
    }
    .stat-icon {
      width: 50px;
      height: 50px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      mat-icon { font-size: 26px; width: 26px; height: 26px; }
      &.icon-solde { background: #e0f2fe; color: #0284c7; }
      &.icon-pending { background: #fef3c7; color: #d97706; }
      &.icon-approved { background: #dcfce7; color: #16a34a; }
      &.icon-team { background: #f3e8ff; color: #9333ea; }
    }
    .stat-content {
      display: flex;
      flex-direction: column;
    }
    .stat-label { font-size: 0.8rem; color: #64748b; font-weight: 500; }
    .stat-val { font-size: 1.4rem; font-weight: 700; color: #1e293b; small { font-size: 0.8rem; color: #94a3b8; } }
    .section-card {
      background: white;
      border: 1px solid var(--gct-border);
      border-radius: 12px;
      padding: 1.5rem;
      h3 { font-size: 1.1rem; color: var(--gct-navy); margin-bottom: 1.25rem; }
    }
    .soldes-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 1rem;
    }
    .solde-item {
      border: 1px solid #e2e8f0;
      border-radius: 8px;
      padding: 1rem;
      background: #f8fafc;
    }
    .solde-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 0.75rem;
    }
    .solde-title { font-weight: 600; font-size: 0.9rem; color: #1e293b; }
    .solde-badge {
      font-size: 0.7rem;
      font-weight: 600;
      padding: 2px 8px;
      border-radius: 12px;
      background: #e2e8f0;
      color: #475569;
      &.auto { background: #dcfce7; color: #15803d; }
    }
    .solde-progress .bar {
      height: 8px;
      background: #e2e8f0;
      border-radius: 4px;
      overflow: hidden;
      margin-bottom: 6px;
    }
    .solde-progress .fill {
      height: 100%;
      background: var(--gct-teal);
    }
    .solde-counts {
      display: flex;
      justify-content: space-between;
      font-size: 0.8rem;
      color: #64748b;
      strong { color: #0f172a; }
    }
    .rules-banner {
      background: #f0fdf4;
      border: 1px solid #bbf7d0;
      border-radius: 12px;
      padding: 1.25rem;
      display: flex;
      gap: 1rem;
    }
    .rule-icon mat-icon { color: #16a34a; font-size: 28px; width: 28px; height: 28px; }
    .rule-text h4 { color: #166534; font-size: 0.95rem; margin-bottom: 6px; }
    .rule-text p { font-size: 0.85rem; color: #15803d; margin-bottom: 4px; line-height: 1.4; }
  `]
})
export class DashboardComponent implements OnInit {
  stats: any = null;
  soldes: SoldeConge[] = [];

  constructor(
    public auth: AuthService,
    public t: TranslationService,
    private congeService: CongeService
  ) {}

  ngOnInit() {
    this.congeService.getDashboardStats().subscribe(data => this.stats = data);
    this.congeService.getMesSoldes().subscribe(data => this.soldes = data);
  }
}
