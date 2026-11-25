import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./features/home/home').then((m) => m.Home) },
  {
    path: 'hoje',
    title: 'Hoje',
    loadComponent: () => import('./features/today/today-page').then((m) => m.TodayPage),
  },
  { path: 'cotacoes', pathMatch: 'full', redirectTo: 'cotacoes/acoes' },
  {
    path: 'cotacoes/:categoria',
    title: 'Cotações',
    loadComponent: () => import('./features/quotes/quotes-page').then((m) => m.QuotesPage),
  },
  {
    path: 'carteira',
    title: 'Carteira',
    loadComponent: () => import('./features/portfolio/portfolio-page').then((m) => m.PortfolioPage),
  },
  {
    path: 'carteira/montar',
    title: 'Montar carteira',
    loadComponent: () => import('./features/portfolio/builder-page').then((m) => m.BuilderPage),
  },
  {
    path: 'ativo/:ticker',
    title: 'Ativo',
    loadComponent: () => import('./features/asset/asset-page').then((m) => m.AssetPage),
  },
  {
    path: 'radar',
    title: 'Radar',
    loadComponent: () => import('./features/watchlist/radar-page').then((m) => m.RadarPage),
  },
  {
    path: 'renda-fixa',
    title: 'Renda fixa',
    loadComponent: () =>
      import('./features/fixed-income/fixed-income-page').then((m) => m.FixedIncomePage),
  },
  { path: '**', redirectTo: '' },
];
