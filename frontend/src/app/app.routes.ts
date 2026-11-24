import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./features/home/home').then((m) => m.Home) },
  {
    path: 'cotacoes',
    title: 'Cotações',
    loadComponent: () => import('./features/quotes/quotes-page').then((m) => m.QuotesPage),
  },
  { path: '**', redirectTo: '' },
];
