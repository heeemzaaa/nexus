import { Routes } from '@angular/router';
import { loginRegisterGuard } from './core/guards/login-register.guard';

export const routes: Routes = [
  {
    path: 'auth',
    canActivate: [loginRegisterGuard],
    loadComponent: () =>
      import('./features/auth/auth.component').then((m) => m.AuthComponent),
    loadChildren: () =>
      import('./features/auth/auth.routes').then((m) => m.routes),
  },
  {
    path: '',
    loadComponent: () =>
      import('./layout/home-layout/home-layout.component').then((m) => m.HomeLayoutComponent),
    loadChildren: () =>
      import('./features/home/home.routes').then((m) => m.routes),
  }
];
