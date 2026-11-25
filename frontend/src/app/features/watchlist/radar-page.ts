import { HttpClient } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  effect,
  inject,
  signal,
  untracked,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { Icon } from '../../shared/icon';
import { brl, percent, tone } from '../../shared/money';
import { TradeService } from '../../shared/trade';
import { WatchlistService } from '../../shared/watchlist';
import { typeColor } from '../quotes/categories';
import { formatVolume } from '../quotes/format';
import { Quote } from '../quotes/quote';

// O Radar lista poucos ativos de categorias diferentes; consultar cada um de tempos em tempos é
// mais simples que manter várias assinaturas STOMP abertas.
const REFRESH_MS = 5000;
const TICKER = /^[A-Z]{4}\d{1,2}[A-Z]?$/;

/** Radar: os ativos que o usuário quer acompanhar de perto, com atalho para negociar. */
@Component({
  selector: 'app-radar-page',
  imports: [RouterLink, Icon],
  templateUrl: './radar-page.html',
  styleUrl: './radar-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RadarPage {
  private readonly http = inject(HttpClient);
  protected readonly watchlist = inject(WatchlistService);
  protected readonly trade = inject(TradeService);

  protected readonly brl = brl;
  protected readonly percent = percent;
  protected readonly tone = tone;
  protected readonly typeColor = typeColor;
  protected readonly formatVolume = formatVolume;

  protected readonly quotes = signal<Readonly<Record<string, Quote>>>({});
  protected readonly draft = signal('');
  protected readonly error = signal<string | null>(null);
  protected readonly adding = signal(false);

  constructor() {
    effect(() => {
      const tickers = this.watchlist.tickers();
      untracked(() => this.refresh(tickers));
    });
    const timer = setInterval(() => this.refresh(this.watchlist.tickers()), REFRESH_MS);
    inject(DestroyRef).onDestroy(() => clearInterval(timer));
  }

  protected add(event: Event): void {
    event.preventDefault();
    const ticker = this.draft().trim().toUpperCase();
    this.error.set(null);
    if (!TICKER.test(ticker)) {
      this.error.set('Use o código de negociação, como PETR4 ou HGLG11.');
      return;
    }
    if (this.watchlist.has(ticker)) {
      this.draft.set('');
      return;
    }
    this.adding.set(true);
    this.http.get<Quote>(`/api/assets/${ticker}`).subscribe({
      next: (quote) => {
        this.adding.set(false);
        this.quotes.update((all) => ({ ...all, [ticker]: quote }));
        this.watchlist.toggle(ticker);
        this.draft.set('');
      },
      error: () => {
        this.adding.set(false);
        this.error.set(`${ticker} não está no catálogo da B3 que carregamos.`);
      },
    });
  }

  private refresh(tickers: readonly string[]): void {
    for (const ticker of tickers) {
      this.http.get<Quote>(`/api/assets/${ticker}`).subscribe({
        next: (quote) => this.quotes.update((all) => ({ ...all, [ticker]: quote })),
        error: () => undefined,
      });
    }
  }
}
