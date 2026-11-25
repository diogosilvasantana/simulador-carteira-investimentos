import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  signal,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map } from 'rxjs';
import { QuoteStream } from './features/quotes/quote-stream';
import { TradeDrawer } from './shared/trade-drawer';
import { BrandMark } from './shared/brand-mark';
import { Icon, IconName } from './shared/icon';

interface NavItem {
  readonly path: string;
  readonly label: string;
  readonly icon: IconName;
  readonly exact: boolean;
}

const COLLAPSED_KEY = 'simulador.nav.collapsed';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, Icon, BrandMark, TradeDrawer],
  templateUrl: './app.html',
  styleUrl: './app.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { '[class.collapsed]': 'collapsed()' },
})
export class App {
  // Só telas com backend de verdade entram no menu.
  protected readonly items: readonly NavItem[] = [
    { path: '/', label: 'Início', icon: 'home', exact: true },
    { path: '/carteira', label: 'Carteira', icon: 'wallet', exact: false },
    { path: '/cotacoes', label: 'Cotações', icon: 'quotes', exact: false },
    { path: '/radar', label: 'Radar', icon: 'radar', exact: false },
    { path: '/renda-fixa', label: 'Renda fixa', icon: 'fixedIncome', exact: false },
  ];

  protected readonly collapsed = signal(readCollapsed());

  private readonly router = inject(Router);
  private readonly url = toSignal(
    this.router.events.pipe(
      filter((event) => event instanceof NavigationEnd),
      map(() => this.router.url),
    ),
    { initialValue: this.router.url },
  );

  protected readonly stream = inject(QuoteStream);
  protected readonly stateLabel = {
    connected: 'Cotações ao vivo',
    connecting: 'Conectando às cotações…',
    disconnected: 'Cotações desconectadas',
  } as const;

  // O fluxo de cotações só abre em Cotações e na página do ativo; fora delas não há conexão cujo
  // estado mostrar.
  protected readonly onQuotes = computed(() =>
    ['/cotacoes', '/ativo'].some((prefix) => this.url().startsWith(prefix)),
  );

  constructor() {
    effect(() => writeCollapsed(this.collapsed()));
  }

  protected toggle(): void {
    this.collapsed.update((value) => !value);
  }
}

// O navegador pode negar o armazenamento (modo privado, política da empresa): o menu só abre.
function readCollapsed(): boolean {
  try {
    return globalThis.localStorage?.getItem(COLLAPSED_KEY) === 'true';
  } catch {
    return false;
  }
}

function writeCollapsed(value: boolean): void {
  try {
    globalThis.localStorage?.setItem(COLLAPSED_KEY, String(value));
  } catch {
    // Sem armazenamento, a escolha vale só nesta aba.
  }
}
