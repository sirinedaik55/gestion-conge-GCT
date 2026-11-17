import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { SidebarComponent } from './shared/components/sidebar/sidebar.component';
import { AuthService } from './core/auth/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterModule, NavbarComponent, SidebarComponent],
  template: `
    <div class="app-container" [class.authenticated]="auth.isLoggedIn()">
      <app-navbar *ngIf="auth.isLoggedIn()"></app-navbar>

      <div class="main-layout" *ngIf="auth.isLoggedIn()">
        <app-sidebar></app-sidebar>
        <main class="content-area">
          <router-outlet></router-outlet>
        </main>
      </div>

      <div class="auth-layout" *ngIf="!auth.isLoggedIn()">
        <router-outlet></router-outlet>
      </div>
    </div>
  `,
  styles: [`
    .app-container {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }
    .main-layout {
      display: flex;
      flex: 1;
    }
    .content-area {
      flex: 1;
      padding: 2rem;
      max-width: 1400px;
      overflow-y: auto;
    }
    .auth-layout {
      flex: 1;
      display: flex;
    }
  `]
})
export class AppComponent {
  constructor(public auth: AuthService) {}
}
