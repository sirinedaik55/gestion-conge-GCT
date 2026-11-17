import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StatutDemande } from '../../../core/models/demande-conge.model';
import { TranslationService } from '../../../core/services/translation.service';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span class="badge-status" [ngClass]="getBadgeClass()">
      <span class="dot"></span>
      {{ getLabel() }}
    </span>
  `,
  styles: [`
    .badge-status {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 4px 10px;
      border-radius: 9999px;
      font-size: 0.75rem;
      font-weight: 600;
      white-space: nowrap;
    }
    .dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background-color: currentColor;
    }
    .badge-en-attente { background-color: #fef3c7; color: #b45309; }
    .badge-approuve-chef { background-color: #e0f2fe; color: #0284c7; }
    .badge-approuve { background-color: #dcfce7; color: #15803d; }
    .badge-rejete { background-color: #fee2e2; color: #b91c1c; }
    .badge-annule { background-color: #f1f5f9; color: #475569; }
  `]
})
export class StatusBadgeComponent {
  @Input() statut!: StatutDemande;

  constructor(private tService: TranslationService) {}

  getBadgeClass(): string {
    switch (this.statut) {
      case 'EN_ATTENTE': return 'badge-en-attente';
      case 'APPROUVE_CHEF': return 'badge-approuve-chef';
      case 'APPROUVE': return 'badge-approuve';
      case 'REJETE': return 'badge-rejete';
      case 'ANNULE': return 'badge-annule';
      default: return '';
    }
  }

  getLabel(): string {
    return this.tService.t(`status_${this.statut}`);
  }
}
