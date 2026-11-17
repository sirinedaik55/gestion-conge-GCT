import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { LoginComponent } from './modules/auth/login/login.component';
import { DashboardComponent } from './modules/dashboard/dashboard.component';
import { ListeCongesComponent } from './modules/conges/liste-conges/liste-conges.component';
import { DemandeCongeComponent } from './modules/conges/demande-conge/demande-conge.component';
import { CalendrierComponent } from './modules/conges/calendrier/calendrier.component';
import { ListeEmployesComponent } from './modules/employes/liste-employes.component';
import { ListeDepartementsComponent } from './modules/departements/liste-departements.component';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'auth/login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'conges', component: ListeCongesComponent, canActivate: [authGuard] },
  { path: 'conges/demande', component: DemandeCongeComponent, canActivate: [authGuard] },
  { path: 'conges/validations', component: ListeCongesComponent, canActivate: [authGuard] },
  { path: 'conges/calendrier', component: CalendrierComponent, canActivate: [authGuard] },
  { path: 'employes', component: ListeEmployesComponent, canActivate: [authGuard] },
  { path: 'departements', component: ListeDepartementsComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: 'dashboard' }
];
