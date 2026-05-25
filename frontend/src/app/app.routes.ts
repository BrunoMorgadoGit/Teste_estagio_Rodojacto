import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { LayoutComponent } from './shared/components/layout/layout.component';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component').then((m) => m.LoginComponent)
  },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./pages/dashboard/dashboard.component').then((m) => m.DashboardComponent)
      },
      {
        path: 'organizations',
        loadComponent: () =>
          import('./pages/organizations/organizations.component').then((m) => m.OrganizationsComponent)
      },
      {
        path: 'collaborators',
        loadComponent: () =>
          import('./pages/collaborators/collaborators.component').then((m) => m.CollaboratorsComponent)
      },
      {
        path: 'devices',
        loadComponent: () => import('./pages/devices/devices.component').then((m) => m.DevicesComponent)
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'dashboard'
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
