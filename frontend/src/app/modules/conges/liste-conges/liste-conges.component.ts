import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { CongeService } from '../../../core/services/conge.service';
import { AuthService } from '../../../core/auth/auth.service';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { DemandeConge, StatutDemande } from '../../../core/models/demande-conge.model';

@Component({
  selector: 'app-liste-conges',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, MatTableModule, MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule, MatSelectModule, StatusBadgeComponent],
  template: `
    <div class="conges-page">
      <div class="page-header">
        <div>
          <h2>Gestion des Demandes de Congé</h2>
          <p>Consultez, suivez et traitez les demandes de congé selon vos autorisations.</p>
        </div>
        <a routerLink="/conges/demande" class="btn-primary">
          <mat-icon>add</mat-icon>
          <span>Nouvelle Demande</span>
        </a>
      </div>

      <!-- Filtres -->
      <div class="filters-bar">
        <div class="filter-group">
          <label>Filtrer par statut :</label>
          <select [(ngModel)]="selectedStatut" (change)="filtrer()">
            <option value="">Tous les statuts</option>
            <option value="EN_ATTENTE">En attente du chef</option>
            <option value="APPROUVE_CHEF">Visé chef (attente RH)</option>
            <option value="APPROUVE">Approuvé</option>
            <option value="REJETE">Rejeté</option>
            <option value="ANNULE">Annulé</option>
          </select>
        </div>

        <div class="search-box">
          <input type="text" [(ngModel)]="searchTerm" (input)="filtrer()" placeholder="Rechercher par collaborateur, département...">
        </div>
      </div>

      <!-- Table des demandes -->
      <div class="table-container">
        <table class="data-table">
          <thead>
            <tr>
              <th>Collaborateur</th>
              <th>Département</th>
              <th>Type de congé</th>
              <th>Période</th>
              <th>Jours</th>
              <th>Statut</th>
              <th>Date dépôt</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let d of filteredDemandes" [class.highlight-row]="d.autoApprouve">
              <td>
                <div class="emp-cell">
                  <strong>{{ d.employePrenom }} {{ d.employeNom }}</strong>
                  <small>{{ d.employeMatricule }}</small>
                </div>
              </td>
              <td>{{ d.departementNom }}</td>
              <td>
                <span class="type-tag" [class.maternite]="d.autoApprouve">
                  {{ d.typeCongeNom }} {{ d.autoApprouve ? '⚡' : '' }}
                </span>
              </td>
              <td>
                <div class="period-cell">
                  <span>{{ d.dateDebut | date:'dd/MM/yyyy' }}</span>
                  <span class="arrow">→</span>
                  <span>{{ d.dateFin | date:'dd/MM/yyyy' }}</span>
                </div>
              </td>
              <td><strong>{{ d.nbJours }}j</strong></td>
              <td>
                <app-status-badge [statut]="d.statut"></app-status-badge>
              </td>
              <td>{{ d.dateSoumission | date:'dd/MM/yyyy HH:mm' }}</td>
              <td>
                <div class="action-buttons">
                  <!-- Visa par Chef de département -->
                  <button *ngIf="d.statut === 'EN_ATTENTE' && auth.hasRole(['CHEF_DEPARTEMENT', 'ADMIN'])"
                          class="btn-action approve-chef"
                          (click)="validerChef(d)"
                          title="Donner avis favorable">
                    <mat-icon>check</mat-icon>
                    <span>Viser</span>
                  </button>

                  <!-- Validation finale par RH -->
                  <button *ngIf="(d.statut === 'APPROUVE_CHEF' || d.statut === 'EN_ATTENTE') && auth.hasRole(['RH', 'ADMIN'])"
                          class="btn-action approve-rh"
                          (click)="validerRh(d)"
                          title="Valider définitivement">
                    <mat-icon>done_all</mat-icon>
                    <span>Valider RH</span>
                  </button>

                  <!-- Rejet -->
                  <button *ngIf="(d.statut === 'EN_ATTENTE' || d.statut === 'APPROUVE_CHEF') && auth.hasRole(['CHEF_DEPARTEMENT', 'RH', 'ADMIN'])"
                          class="btn-action reject"
                          (click)="rejeter(d)"
                          title="Rejeter">
                    <mat-icon>close</mat-icon>
                  </button>

                  <!-- Annulation par l'employé -->
                  <button *ngIf="(d.statut === 'EN_ATTENTE' || d.statut === 'APPROUVE_CHEF') && d.employeId === auth.currentUser()?.id"
                          class="btn-action cancel"
                          (click)="annuler(d)"
                          title="Annuler ma demande">
                    <mat-icon>cancel</mat-icon>
                  </button>
                </div>
              </td>
            </tr>

            <tr *ngIf="filteredDemandes.length === 0">
              <td colspan="8" class="empty-cell">Aucune demande trouvée avec les critères sélectionnés.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .conges-page {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 1rem;
      h2 { font-size: 1.4rem; color: var(--gct-navy); }
      p { color: #64748b; font-size: 0.9rem; }
    }
    .btn-primary {
      background: var(--gct-blue);
      color: white;
      text-decoration: none;
      padding: 0.65rem 1.2rem;
      border-radius: 8px;
      display: flex;
      align-items: center;
      gap: 6px;
      font-weight: 600;
      transition: background 0.2s;
      &:hover { background: var(--gct-navy); }
    }
    .filters-bar {
      background: white;
      border: 1px solid var(--gct-border);
      border-radius: 10px;
      padding: 1rem 1.25rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 1rem;
    }
    .filter-group {
      display: flex;
      align-items: center;
      gap: 8px;
      label { font-size: 0.85rem; font-weight: 600; color: #4a5568; }
      select {
        border: 1px solid #cbd5e0;
        border-radius: 6px;
        padding: 6px 12px;
        font-size: 0.85rem;
        background: white;
      }
    }
    .search-box input {
      border: 1px solid #cbd5e0;
      border-radius: 6px;
      padding: 8px 14px;
      font-size: 0.85rem;
      min-width: 280px;
    }
    .table-container {
      background: white;
      border: 1px solid var(--gct-border);
      border-radius: 12px;
      overflow-x: auto;
    }
    .data-table {
      width: 100%;
      border-collapse: collapse;
      text-align: left;
      font-size: 0.88rem;
    }
    .data-table th {
      background: #f8fafc;
      padding: 12px 16px;
      font-weight: 600;
      color: #475569;
      border-bottom: 1px solid #e2e8f0;
    }
    .data-table td {
      padding: 12px 16px;
      border-bottom: 1px solid #f1f5f9;
      color: #334155;
    }
    .emp-cell {
      display: flex;
      flex-direction: column;
      small { color: #94a3b8; font-size: 0.75rem; }
    }
    .type-tag {
      background: #f1f5f9;
      padding: 3px 8px;
      border-radius: 6px;
      font-size: 0.8rem;
      color: #334155;
      &.maternite { background: #fdf2f8; color: #db2777; font-weight: 600; }
    }
    .period-cell {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 0.82rem;
      .arrow { color: #94a3b8; }
    }
    .action-buttons {
      display: flex;
      gap: 6px;
    }
    .btn-action {
      border: none;
      border-radius: 6px;
      padding: 4px 8px;
      display: inline-flex;
      align-items: center;
      gap: 4px;
      font-size: 0.75rem;
      font-weight: 600;
      cursor: pointer;
      mat-icon { font-size: 16px; width: 16px; height: 16px; }
      &.approve-chef { background: #e0f2fe; color: #0284c7; &:hover { background: #bae6fd; } }
      &.approve-rh { background: #dcfce7; color: #16a34a; &:hover { background: #bbf7d0; } }
      &.reject { background: #fee2e2; color: #dc2626; &:hover { background: #fca5a5; } }
      &.cancel { background: #f1f5f9; color: #64748b; &:hover { background: #e2e8f0; } }
    }
    .empty-cell {
      text-align: center;
      padding: 2rem !important;
      color: #94a3b8;
    }
  `]
})
export class ListeCongesComponent implements OnInit {
  demandes: DemandeConge[] = [];
  filteredDemandes: DemandeConge[] = [];
  selectedStatut = '';
  searchTerm = '';

  constructor(public auth: AuthService, private congeService: CongeService) {}

  ngOnInit() {
    this.chargerDemandes();
  }

  chargerDemandes() {
    this.congeService.getDemandes().subscribe(data => {
      this.demandes = data;
      this.filtrer();
    });
  }

  filtrer() {
    this.filteredDemandes = this.demandes.filter(d => {
      const matchStatut = !this.selectedStatut || d.statut === this.selectedStatut;
      const term = this.searchTerm.toLowerCase();
      const matchSearch = !term ||
        d.employeNom.toLowerCase().includes(term) ||
        d.employePrenom.toLowerCase().includes(term) ||
        d.departementNom.toLowerCase().includes(term) ||
        d.typeCongeNom.toLowerCase().includes(term);
      return matchStatut && matchSearch;
    });
  }

  validerChef(d: DemandeConge) {
    if (confirm(`Accorder l'avis favorable pour la demande de ${d.employePrenom} ${d.employeNom} ?`)) {
      this.congeService.validerParChef(d.id, "Avis favorable accordé.").subscribe(() => {
        this.chargerDemandes();
      });
    }
  }

  validerRh(d: DemandeConge) {
    if (confirm(`Valider définitivement le congé de ${d.employePrenom} ${d.employeNom} (${d.nbJours} jours) ?`)) {
      this.congeService.validerParRh(d.id, "Validation RH accordée.").subscribe(() => {
        this.chargerDemandes();
      });
    }
  }

  rejeter(d: DemandeConge) {
    const motif = prompt("Veuillez indiquer le motif du refus :");
    if (motif !== null && motif.trim()) {
      this.congeService.rejeter(d.id, motif).subscribe(() => {
        this.chargerDemandes();
      });
    }
  }

  annuler(d: DemandeConge) {
    if (confirm("Êtes-vous sûr de vouloir annuler votre demande de congé ?")) {
      this.congeService.annuler(d.id).subscribe(() => {
        this.chargerDemandes();
      });
    }
  }
}
