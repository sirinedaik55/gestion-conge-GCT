import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { CongeService } from '../../../core/services/conge.service';
import { DemandeConge } from '../../../core/models/demande-conge.model';

@Component({
  selector: 'app-calendrier',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule],
  template: `
    <div class="calendar-page">
      <div class="page-header">
        <div>
          <h2>Planning Mensuel des Congés & Absences</h2>
          <p>Visualisation des congés validés par département pour vérifier la règle des 2 personnes max.</p>
        </div>
        <div class="month-nav">
          <button (click)="changerMois(-1)"><mat-icon>chevron_left</mat-icon></button>
          <span class="current-month">{{ moisActuelNom }} {{ anneeActuelle }}</span>
          <button (click)="changerMois(1)"><mat-icon>chevron_right</mat-icon></button>
        </div>
      </div>

      <div class="calendar-container">
        <!-- Grille du calendrier -->
        <div class="calendar-grid">
          <div *ngFor="let jour of joursSemaine" class="day-header">{{ jour }}</div>
          <div *ngFor="let cell of cellulesMois" class="day-cell" [class.today]="cell.estAujourdhui" [class.other-month]="!cell.duMois">
            <span class="day-num">{{ cell.numero }}</span>
            <div class="events-list">
              <div *ngFor="let conge of cell.conges" class="event-pill" [title]="conge.employePrenom + ' ' + conge.employeNom + ' (' + conge.departementNom + ')'">
                <span class="event-name">{{ conge.employePrenom[0] }}. {{ conge.employeNom }}</span>
                <span class="event-dept">{{ conge.departementNom }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .calendar-page {
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
    .month-nav {
      display: flex;
      align-items: center;
      gap: 1rem;
      background: white;
      border: 1px solid var(--gct-border);
      border-radius: 8px;
      padding: 4px 12px;
      button {
        background: none;
        border: none;
        cursor: pointer;
        display: flex;
        align-items: center;
        color: #475569;
        &:hover { color: var(--gct-navy); }
      }
      .current-month { font-weight: 700; color: #1e293b; min-width: 140px; text-align: center; }
    }
    .calendar-container {
      background: white;
      border: 1px solid var(--gct-border);
      border-radius: 12px;
      padding: 1.5rem;
      overflow-x: auto;
    }
    .calendar-grid {
      display: grid;
      grid-template-columns: repeat(7, 1fr);
      gap: 1px;
      background: #e2e8f0;
      border: 1px solid #e2e8f0;
      border-radius: 8px;
      overflow: hidden;
    }
    .day-header {
      background: #f8fafc;
      padding: 10px;
      text-align: center;
      font-weight: 700;
      font-size: 0.8rem;
      color: #475569;
    }
    .day-cell {
      background: white;
      min-height: 100px;
      padding: 6px;
      display: flex;
      flex-direction: column;
      gap: 4px;
      &.other-month { background: #f8fafc; opacity: 0.5; }
      &.today { background: #eff6ff; border: 2px solid #3b82f6; }
    }
    .day-num { font-size: 0.8rem; font-weight: 600; color: #64748b; }
    .events-list {
      display: flex;
      flex-direction: column;
      gap: 3px;
    }
    .event-pill {
      background: #e0f2fe;
      color: #0369a1;
      padding: 2px 6px;
      border-radius: 4px;
      font-size: 0.7rem;
      display: flex;
      flex-direction: column;
      line-height: 1.1;
      overflow: hidden;
      cursor: pointer;
    }
    .event-name { font-weight: 600; white-space: nowrap; text-overflow: ellipsis; }
    .event-dept { font-size: 0.65rem; color: #0284c7; white-space: nowrap; }
  `]
})
export class CalendrierComponent implements OnInit {
  joursSemaine = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];
  moisNoms = ['Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin', 'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'];
  dateCourante = new Date();
  moisActuelNom = '';
  anneeActuelle = 2024;
  cellulesMois: any[] = [];
  congesApprouves: DemandeConge[] = [];

  constructor(private congeService: CongeService) {}

  ngOnInit() {
    this.mettreAJourMois();
  }

  mettreAJourMois() {
    const mois = this.dateCourante.getMonth();
    this.anneeActuelle = this.dateCourante.getFullYear();
    this.moisActuelNom = this.moisNoms[mois];

    const debutMois = new Date(this.anneeActuelle, mois, 1);
    const finMois = new Date(this.anneeActuelle, mois + 1, 0);

    const debutStr = debutMois.toISOString().split('T')[0];
    const finStr = finMois.toISOString().split('T')[0];

    this.congeService.getCalendrier(debutStr, finStr).subscribe(conges => {
      this.congesApprouves = conges;
      this.construireGrille(debutMois, finMois);
    });
  }

  construireGrille(debutMois: Date, finMois: Date) {
    this.cellulesMois = [];
    const annee = debutMois.getFullYear();
    const mois = debutMois.getMonth();

    // Trouver le jour de la semaine du premier jour (0 = Dimanche -> on veut Lundi = 0)
    let premierJourIndex = debutMois.getDay() - 1;
    if (premierJourIndex === -1) premierJourIndex = 6;

    // Jours du mois précédent
    const nbJoursMoisPrec = new Date(annee, mois, 0).getDate();
    for (let i = premierJourIndex - 1; i >= 0; i--) {
      this.cellulesMois.push({
        numero: nbJoursMoisPrec - i,
        duMois: false,
        conges: []
      });
    }

    // Jours du mois en cours
    const aujourdhui = new Date();
    for (let d = 1; d <= finMois.getDate(); d++) {
      const dateCellule = new Date(annee, mois, d);
      const congesDuJour = this.congesApprouves.filter(c => {
        const deb = new Date(c.dateDebut);
        const fn = new Date(c.dateFin);
        return dateCellule >= deb && dateCellule <= fn;
      });

      this.cellulesMois.push({
        numero: d,
        duMois: true,
        estAujourdhui: aujourdhui.getFullYear() === annee && aujourdhui.getMonth() === mois && aujourdhui.getDate() === d,
        conges: congesDuJour
      });
    }

    // Compléter avec le mois suivant pour faire 35 ou 42 cases
    const totalCases = Math.ceil(this.cellulesMois.length / 7) * 7;
    const reste = totalCases - this.cellulesMois.length;
    for (let j = 1; j <= reste; j++) {
      this.cellulesMois.push({
        numero: j,
        duMois: false,
        conges: []
      });
    }
  }

  changerMois(delta: number) {
    this.dateCourante.setMonth(this.dateCourante.getMonth() + delta);
    this.mettreAJourMois();
  }
}
