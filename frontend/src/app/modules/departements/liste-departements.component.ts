import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { DepartementService } from '../../core/services/departement.service';
import { Departement } from '../../core/models/departement.model';

@Component({
  selector: 'app-liste-departements',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule],
  template: `
    <div class="dept-page">
      <div class="page-header">
        <div>
          <h2>Unités & Départements GCT</h2>
          <p>Paramétrage des plafonds d'absences simultanées par unité de production ou service.</p>
        </div>
      </div>

      <div class="dept-grid">
        <div *ngFor="let d of departements" class="dept-card">
          <div class="dept-header">
            <span class="dept-code">{{ d.code }}</span>
            <div class="quota-pill" title="Règle GCT d'absences simultanées autorisées">
              <mat-icon>group</mat-icon>
              <span>Max {{ d.maxAbsencesSimultanees }} abs. simultanées</span>
            </div>
          </div>
          <h3>{{ d.nom }}</h3>
          <p class="dept-desc">{{ d.description || 'Unité industrielle ou direction Groupe Chimique Tunisien' }}</p>
          <div class="dept-footer">
            <span class="emp-count">Effectif affecté : <strong>{{ d.totalEmployes ?? 0 }} employés</strong></span>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dept-page {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }
    .page-header h2 { font-size: 1.4rem; color: var(--gct-navy); }
    .page-header p { color: #64748b; font-size: 0.9rem; }
    .dept-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: 1.25rem;
    }
    .dept-card {
      background: white;
      border: 1px solid var(--gct-border);
      border-radius: 12px;
      padding: 1.5rem;
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }
    .dept-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .dept-code {
      background: #f1f5f9;
      color: #334155;
      font-weight: 700;
      font-size: 0.75rem;
      padding: 2px 8px;
      border-radius: 4px;
    }
    .quota-pill {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      background: #e0f2fe;
      color: #0369a1;
      padding: 3px 8px;
      border-radius: 12px;
      font-size: 0.75rem;
      font-weight: 600;
      mat-icon { font-size: 16px; width: 16px; height: 16px; }
    }
    .dept-card h3 { font-size: 1.1rem; color: var(--gct-navy); margin-top: 4px; }
    .dept-desc { font-size: 0.85rem; color: #64748b; line-height: 1.4; flex: 1; }
    .dept-footer {
      border-top: 1px solid #f1f5f9;
      padding-top: 0.75rem;
      font-size: 0.82rem;
      color: #475569;
    }
  `]
})
export class ListeDepartementsComponent implements OnInit {
  departements: Departement[] = [];

  constructor(private departementService: DepartementService) {}

  ngOnInit() {
    this.departementService.getAll().subscribe(data => this.departements = data);
  }
}
