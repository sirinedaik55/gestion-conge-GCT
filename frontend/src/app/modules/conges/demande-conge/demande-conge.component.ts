import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CongeService } from '../../../core/services/conge.service';
import { TypeConge } from '../../../core/models/demande-conge.model';

@Component({
  selector: 'app-demande-conge',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatIconModule],
  template: `
    <div class="demande-page">
      <div class="page-header">
        <a routerLink="/conges" class="back-link">
          <mat-icon>arrow_back</mat-icon>
          <span>Retour à la liste</span>
        </a>
        <h2>Nouvelle Demande de Congé</h2>
        <p>Remplissez le formulaire ci-dessous selon les règles en vigueur au Groupe Chimique Tunisien.</p>
      </div>

      <div *ngIf="errorMessage" class="alert-box error">
        <mat-icon>error_outline</mat-icon>
        <div>
          <strong>Demande non recevable :</strong>
          <p>{{ errorMessage }}</p>
        </div>
      </div>

      <div *ngIf="successMessage" class="alert-box success">
        <mat-icon>check_circle</mat-icon>
        <div>
          <strong>Demande enregistrée avec succès !</strong>
          <p>{{ successMessage }}</p>
        </div>
      </div>

      <div class="form-container">
        <!-- Bannière explicative pour le congé de maternité -->
        <div *ngIf="selectedType?.autoApprouve" class="maternity-alert">
          <div class="maternity-badge">RÈGLE SPÉCIALE GCT</div>
          <h4>Congé de Maternité (Acceptation Automatique)</h4>
          <p>Conformément au règlement du Groupe Chimique Tunisien, ce congé est <strong>validé et approuvé immédiatement</strong> dès soumission, sans aucune attente ni besoin de visa préalable de votre chef de département ou de la DRH.</p>
        </div>

        <form (ngSubmit)="soumettre()" class="conge-form">
          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Type de congé</mat-label>
            <mat-select [(ngModel)]="typeCongeId" name="typeCongeId" required (selectionChange)="onTypeChange()">
              <mat-option *ngFor="let t of typesConge" [value]="t.id">
                {{ t.nom }} (Max: {{ t.joursMaxParAn }}j/an) {{ t.autoApprouve ? '⚡ Auto-approuvé' : '' }}
              </mat-option>
            </mat-select>
          </mat-form-field>

          <div class="dates-row">
            <mat-form-field appearance="outline" class="w-half">
              <mat-label>Date de début</mat-label>
              <input matInput type="date" [(ngModel)]="dateDebut" name="dateDebut" required (change)="calculerJours()">
            </mat-form-field>

            <mat-form-field appearance="outline" class="w-half">
              <mat-label>Date de fin</mat-label>
              <input matInput type="date" [(ngModel)]="dateFin" name="dateFin" required (change)="calculerJours()">
            </mat-form-field>
          </div>

          <!-- Estimation des jours ouvrés calculés -->
          <div *ngIf="joursEstimes > 0" class="jours-pill">
            <mat-icon>date_range</mat-icon>
            <span>Durée calculée : <strong>{{ joursEstimes }} jour(s) ouvré(s)</strong> (hors samedi et dimanche)</span>
          </div>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Motif ou précisions (facultatif)</mat-label>
            <textarea matInput [(ngModel)]="motif" name="motif" rows="3" placeholder="Indiquez ici tout commentaire utile..."></textarea>
          </mat-form-field>

          <div class="form-rules-reminder">
            <mat-icon>info</mat-icon>
            <span>
              <strong>Rappel de la politique GCT :</strong> Pas plus de 2 collaborateurs du même département ne peuvent être absents simultanément. Votre demande sera vérifiée automatiquement contre ce quota.
            </span>
          </div>

          <div class="form-actions">
            <button mat-button type="button" routerLink="/conges" [disabled]="loading">Annuler</button>
            <button mat-raised-button color="primary" type="submit" [disabled]="loading || !typeCongeId || !dateDebut || !dateFin">
              <span *ngIf="!loading">{{ selectedType?.autoApprouve ? 'Soumettre (Validation Immédiate)' : 'Soumettre pour Visa' }}</span>
              <span *ngIf="loading">Traitement en cours...</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .demande-page {
      max-width: 800px;
      margin: 0 auto;
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }
    .back-link {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      color: var(--gct-blue);
      text-decoration: none;
      font-weight: 500;
      margin-bottom: 0.5rem;
    }
    .page-header h2 { font-size: 1.4rem; color: var(--gct-navy); }
    .page-header p { color: #64748b; font-size: 0.9rem; }
    .alert-box {
      border-radius: 8px;
      padding: 1rem 1.25rem;
      display: flex;
      gap: 1rem;
      align-items: flex-start;
      &.error { background: #fee2e2; border: 1px solid #fca5a5; color: #991b1b; }
      &.success { background: #dcfce7; border: 1px solid #86efac; color: #166534; }
    }
    .maternity-alert {
      background: #fdf2f8;
      border: 1px solid #fbcfe8;
      border-radius: 10px;
      padding: 1.25rem;
      margin-bottom: 1.5rem;
    }
    .maternity-badge {
      display: inline-block;
      background: #db2777;
      color: white;
      font-size: 0.7rem;
      font-weight: 700;
      padding: 2px 8px;
      border-radius: 12px;
      margin-bottom: 6px;
    }
    .maternity-alert h4 { color: #9d174d; margin-bottom: 4px; }
    .maternity-alert p { color: #831843; font-size: 0.85rem; line-height: 1.4; }
    .form-container {
      background: white;
      border: 1px solid var(--gct-border);
      border-radius: 12px;
      padding: 2rem;
    }
    .conge-form {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }
    .w-full { width: 100%; }
    .dates-row {
      display: flex;
      gap: 1rem;
    }
    .w-half { flex: 1; }
    .jours-pill {
      background: #e0f2fe;
      color: #0369a1;
      border-radius: 8px;
      padding: 0.75rem 1rem;
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 0.9rem;
      margin-bottom: 0.5rem;
    }
    .form-rules-reminder {
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      padding: 0.75rem 1rem;
      border-radius: 8px;
      display: flex;
      gap: 8px;
      color: #475569;
      font-size: 0.82rem;
      align-items: center;
    }
    .form-actions {
      display: flex;
      justify-content: flex-end;
      gap: 1rem;
      margin-top: 1rem;
    }
  `]
})
export class DemandeCongeComponent implements OnInit {
  typesConge: TypeConge[] = [];
  typeCongeId: number | null = null;
  selectedType: TypeConge | null = null;
  dateDebut = '';
  dateFin = '';
  motif = '';
  joursEstimes = 0;
  loading = false;
  errorMessage = '';
  successMessage = '';

  constructor(private congeService: CongeService, private router: Router) {}

  ngOnInit() {
    this.congeService.getTypesConge().subscribe(types => {
      this.typesConge = types;
    });
  }

  onTypeChange() {
    this.selectedType = this.typesConge.find(t => t.id === this.typeCongeId) || null;
  }

  calculerJours() {
    if (!this.dateDebut || !this.dateFin) {
      this.joursEstimes = 0;
      return;
    }
    const d1 = new Date(this.dateDebut);
    const d2 = new Date(this.dateFin);
    if (d2 < d1) {
      this.joursEstimes = 0;
      return;
    }
    let count = 0;
    const cur = new Date(d1);
    while (cur <= d2) {
      const day = cur.getDay();
      if (day !== 0 && day !== 6) { // Pas Samedi ni Dimanche
        count++;
      }
      cur.setDate(cur.getDate() + 1);
    }
    this.joursEstimes = count;
  }

  soumettre() {
    if (!this.typeCongeId || !this.dateDebut || !this.dateFin) return;
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.congeService.soumettreDemande({
      typeCongeId: this.typeCongeId,
      dateDebut: this.dateDebut,
      dateFin: this.dateFin,
      motif: this.motif
    }).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.statut === 'APPROUVE') {
          this.successMessage = 'Votre congé de maternité a été validé et approuvé automatiquement !';
        } else {
          this.successMessage = 'Demande soumise avec succès ! Elle est transmise à votre chef de département.';
        }
        setTimeout(() => this.router.navigate(['/conges']), 2000);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Erreur lors de la soumission de la demande.';
      }
    });
  }
}
