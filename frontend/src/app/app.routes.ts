import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./features/home/home').then((m) => m.Home) },
  { path: 'cotacoes', pathMatch: 'full', redirectTo: 'cotacoes/acoes' },
  {
    path: 'cotacoes/:categoria',
    title: 'Cotações',
    loadComponent: () => import('./features/quotes/quotes-page').then((m) => m.QuotesPage),
  },
  {
    path: 'renda-fixa',
    title: 'Renda fixa',
    loadComponent: () =>
      import('./features/fixed-income/fixed-income-page').then((m) => m.FixedIncomePage),
  },
  { path: '**', redirectTo: '' },
];
