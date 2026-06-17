import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', loadComponent: () => import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent) },
  { path: 'cartoes', loadComponent: () => import('./pages/cartoes/cartoes.component').then(m => m.CartoesComponent) },
  { path: 'lancamentos', loadComponent: () => import('./pages/lancamentos/lancamentos-lista.component').then(m => m.LancamentosListaComponent) },
  { path: 'configuracoes', loadComponent: () => import('./pages/configuracoes/configuracoes.component').then(m => m.ConfiguracoesComponent) },
  { path: '**', redirectTo: 'dashboard' }
];
