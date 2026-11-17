import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { UtilisateurService } from '../../core/services/utilisateur.service';
import { DepartementService } from '../../core/services/departement.service';
import { Utilisateur } from '../../core/models/utilisateur.model';
import { Departement } from '../../core/models/departement.model';

@Component({
  selector: 'app-liste-employes',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, MatButtonModule],
  template: `
    <div class="employes-page">
      <div class="page-header">
        <div>
          <h2>Gestion des Personnels GCT</h2>
          <p>Administration des collaborateurs, affectations et rôles au sein du groupe.</p>
        </div>
      </div>

      <div class="table-container">
        <table class="data-table">
          <thead>
            <tr>
              <th>Matricule</th>
              <th>Nom & Prénom</th>
              <th>Email</th>
              <th>Département</th>
              <th>Poste</th>
              <th>Rôle</th>
              <th>Statut</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let emp of employes">
              <td><strong>{{ emp.matricule }}</strong></td>
              <td>{{ emp.prenom }} {{ emp.nom }}</td>
              <td>{{ emp.email }}</td>
              <td>{{ emp.departementNom }}</td>
              <td>{{ emp.poste || '—' }}</td>
              <td>
                <span class="role-badge" [ngClass]="emp.role.toLowerCase()">{{ emp.role }}</span>
              </td>
              <td>
                <span class="status-pill" [class.active]="emp.actif" [class.inactive]="!emp.actif">
                  {{ emp.actif ? 'Actif' : 'Désactivé' }}
                </span>
              </td>
              <td>
                <button class="btn-toggle" (click)="toggleStatut(emp)" [title]="emp.actif ? 'Désactiver le compte' : 'Activer le compte'">
                  <mat-icon>{{ emp.actif ? 'block' : 'check_circle' }}</mat-icon>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .employes-page {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }
    .page-header h2 { font-size: 1.4rem; color: var(--gct-navy); }
    .page-header p { color: #64748b; font-size: 0.9rem; }
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
    .role-badge {
      font-size: 0.75rem;
      font-weight: 700;
      padding: 2px 8px;
      border-radius: 4px;
      &.admin { background: #fee2e2; color: #991b1b; }
      &.rh { background: #fef3c7; color: #92400e; }
      &.chef_departement { background: #e0f2fe; color: #0369a1; }
      &.employe { background: #dcfce7; color: #166534; }
    }
    .status-pill {
      font-size: 0.75rem;
      font-weight: 600;
      padding: 2px 8px;
      border-radius: 12px;
      &.active { background: #dcfce7; color: #15803d; }
      &.inactive { background: #fee2e2; color: #b91c1c; }
    }
    .btn-toggle {
      background: none;
      border: none;
      cursor: pointer;
      color: #64748b;
      &:hover { color: var(--gct-navy); }
    }
  `]
})
export class ListeEmployesComponent implements OnInit {
  employes: Utilisateur[] = [];

  constructor(private utilisateurService: UtilisateurService) {}

  ngOnInit() {
    this.charger();
  }

  charger() {
    this.utilisateurService.getAll().subscribe(data => this.employes = data);
  }

  toggleStatut(emp: Utilisateur) {
    this.utilisateurService.toggleStatut(emp.id).subscribe(() => {
      emp.actif = !emp.actif;
    });
  }
}
