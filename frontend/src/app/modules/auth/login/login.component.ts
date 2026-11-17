import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../core/auth/auth.service';
import { TranslationService } from '../../../core/services/translation.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  template: `
    <div class="login-container">
      <div class="lang-switcher">
        <button class="lang-toggle-btn" (click)="t.toggleLanguage()">
          {{ t.currentLang() === 'fr' ? '🇹🇳 العربية' : '🇫🇷 Français' }}
        </button>
      </div>

      <div class="login-card">
        <div class="login-header">
          <div class="logo-circle">GCT</div>
          <h2>{{ t.t('app_title') }}</h2>
          <p>{{ t.t('app_subtitle') }}</p>
        </div>

        <div *ngIf="errorMessage" class="error-banner">
          <mat-icon>error_outline</mat-icon>
          <span>{{ errorMessage }}</span>
        </div>

        <form (ngSubmit)="onSubmit()" class="login-form">
          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Email professionnel</mat-label>
            <input matInput type="email" [(ngModel)]="email" name="email" required placeholder="nom@gct.tn">
            <mat-icon matPrefix>email</mat-icon>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Mot de passe</mat-label>
            <input matInput [type]="hidePassword ? 'password' : 'text'" [(ngModel)]="password" name="password" required>
            <mat-icon matPrefix>lock</mat-icon>
            <button mat-icon-button matSuffix type="button" (click)="hidePassword = !hidePassword">
              <mat-icon>{{ hidePassword ? 'visibility_off' : 'visibility' }}</mat-icon>
            </button>
          </mat-form-field>

          <button mat-raised-button color="primary" class="submit-btn" type="submit" [disabled]="loading">
            <span *ngIf="!loading">Se connecter</span>
            <span *ngIf="loading">Connexion en cours...</span>
          </button>
        </form>

        <!-- Comptes de démonstration pré-configurés -->
        <div class="demo-accounts">
          <p class="demo-title">Comptes de test (mot de passe: <code>password123</code>) :</p>
          <div class="demo-chips">
            <button type="button" (click)="fillCreds('syrine.employe@gct.tn')">Employé (Syrine)</button>
            <button type="button" (click)="fillCreds('chef.mdhilla@gct.tn')">Chef d'unité</button>
            <button type="button" (click)="fillCreds('rh@gct.tn')">Direction RH</button>
            <button type="button" (click)="fillCreds('admin@gct.tn')">Admin</button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .login-container {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #0f3057 0%, #00587a 100%);
      padding: 1.5rem;
      position: relative;
    }
    .lang-switcher {
      position: absolute;
      top: 1.5rem;
      right: 1.5rem;
    }
    .lang-toggle-btn {
      background: rgba(255, 255, 255, 0.2);
      color: white;
      border: 1px solid rgba(255, 255, 255, 0.3);
      padding: 8px 16px;
      border-radius: 20px;
      cursor: pointer;
      font-weight: 600;
      backdrop-filter: blur(4px);
    }
    .login-card {
      width: 100%;
      max-width: 440px;
      background: white;
      border-radius: 16px;
      box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.2), 0 10px 10px -5px rgba(0, 0, 0, 0.1);
      padding: 2.5rem 2rem;
    }
    .login-header {
      text-align: center;
      margin-bottom: 2rem;
    }
    .logo-circle {
      width: 60px;
      height: 60px;
      border-radius: 14px;
      background: linear-gradient(135deg, var(--gct-navy), var(--gct-teal));
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 800;
      font-size: 1.4rem;
      margin: 0 auto 1rem;
      letter-spacing: 1px;
    }
    .login-header h2 {
      font-size: 1.35rem;
      color: var(--gct-navy);
      font-weight: 700;
    }
    .login-header p {
      font-size: 0.85rem;
      color: #718096;
      margin-top: 4px;
    }
    .error-banner {
      background: #fee2e2;
      border: 1px solid #fca5a5;
      color: #991b1b;
      padding: 10px 14px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 1.5rem;
      font-size: 0.85rem;
    }
    .login-form {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }
    .w-full { width: 100%; }
    .submit-btn {
      height: 48px;
      font-size: 1rem;
      font-weight: 600;
      background-color: var(--gct-blue) !important;
      color: white !important;
      border-radius: 8px;
      margin-top: 0.5rem;
    }
    .demo-accounts {
      margin-top: 2rem;
      padding-top: 1.5rem;
      border-top: 1px solid #e2e8f0;
      text-align: center;
    }
    .demo-title {
      font-size: 0.78rem;
      color: #718096;
      margin-bottom: 8px;
    }
    .demo-chips {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      justify-content: center;
    }
    .demo-chips button {
      background: #edf2f7;
      border: 1px solid #cbd5e0;
      padding: 4px 10px;
      border-radius: 12px;
      font-size: 0.75rem;
      cursor: pointer;
      color: #4a5568;
      &:hover { background: #e2e8f0; }
    }
  `]
})
export class LoginComponent {
  email = 'syrine.employe@gct.tn';
  password = 'password123';
  hidePassword = true;
  loading = false;
  errorMessage = '';

  constructor(
    private auth: AuthService,
    private router: Router,
    public t: TranslationService
  ) {}

  fillCreds(email: string) {
    this.email = email;
    this.password = 'password123';
  }

  onSubmit() {
    if (!this.email || !this.password) return;
    this.loading = true;
    this.errorMessage = '';

    this.auth.login({ email: this.email, password: this.password }).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Identifiants invalides';
      }
    });
  }
}
