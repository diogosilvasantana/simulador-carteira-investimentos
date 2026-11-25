import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  effect,
  inject,
  input,
  signal,
  untracked,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { PortfolioService } from '../../core/portfolio';
import { Icon } from '../../shared/icon';
import { brl, percent, signedBrl, tone } from '../../shared/money';
import { TradeService } from '../../shared/trade';
import { WatchlistService } from '../../shared/watchlist';
import { CATEGORIES, LISTING_LABELS, TYPE_LABELS, typeColor } from '../quotes/categories';
import { formatVolume } from '../quotes/format';
import { Quote } from '../quotes/quote';
import { QuoteStream } from '../quotes/quote-stream';

interface Point {
  readonly at: number;
  readonly price: number;
}

const MAX_POINTS = 240;

/** Página do ativo: preço ao vivo, números do dia, a sua posição e os atalhos para negociar. */
@Component({
  selector: 'app-asset-page',
  imports: [RouterLink, DatePipe, Icon],
  templateUrl: './asset-page.html',
  styleUrl: './asset-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AssetPage {
  readonly ticker = input.required<string>();

  private readonly http = inject(HttpClient);
  private readonly stream = inject(QuoteStream);
  protected readonly trade = inject(TradeService);
  protected readonly watchlist = inject(WatchlistService);
  protected readonly portfolio = inject(PortfolioService);

  protected readonly brl = brl;
  protected readonly percent = percent;
  protected readonly signedBrl = signedBrl;
  protected readonly tone = tone;
  protected readonly formatVolume = formatVolume;
  protected readonly typeColor = typeColor;

  protected readonly quote = signal<Quote | null>(null);
  protected readonly missing = signal(false);
  protected readonly revision = signal(0);
  private readonly points = signal<readonly Point[]>([]);

  protected readonly symbol = computed(() => this.ticker().toUpperCase());
  protected readonly category = computed(() =>
    CATEGORIES.find((c) => c.slug === this.quote()?.category),
  );
  protected readonly kind = computed(() => {
    const q = this.quote();
    if (!q) {
      return '';
    }
    const type = q.type ? TYPE_LABELS[q.type] : '';
    const segment = q.listingSegment ? LISTING_LABELS[q.listingSegment] : '';
    return [type, segment].filter(Boolean).join(' · ');
  });
  protected readonly position = computed(() =>
    this.portfolio.summary()?.positions.find((p) => p.ticker === this.symbol()),
  );

  /** Linha do preço desde que a página abriu, desenhada a partir dos ticks recebidos. */
  protected readonly path = computed(() => {
    const pts = this.points();
    if (pts.length < 2) {
      return null;
    }
    const prices = pts.map((p) => p.price);
    const min = Math.min(...prices);
    const max = Math.max(...prices);
    const span = max - min || 1;
    const width = 640;
    const height = 160;
    return pts
      .map((p, i) => {
        const x = (i / (pts.length - 1)) * width;
        const y = height - 8 - ((p.price - min) / span) * (height - 16);
        return `${i ? 'L' : 'M'}${x.toFixed(1)} ${y.toFixed(1)}`;
      })
      .join(' ');
  });
  protected readonly pointCount = computed(() => this.points().length);

  constructor() {
    this.portfolio.load();
    effect(() => {
      const ticker = this.symbol();
      untracked(() => this.open(ticker));
    });
    inject(DestroyRef).onDestroy(() => this.stream.stop());
  }

  private open(ticker: string): void {
    this.quote.set(null);
    this.missing.set(false);
    this.points.set([]);
    this.http.get<Quote>(`/api/assets/${ticker}`).subscribe({
      next: (quote) => {
        this.quote.set(quote);
        this.push(quote);
      },
      error: () => this.missing.set(true),
    });
    this.stream.follow(`/topic/quotes/${ticker}`, (batch) => {
      for (const quote of batch) {
        this.quote.update((current) => ({ ...(current ?? quote), ...quote }));
        this.revision.update((r) => r + 1);
        this.push(quote);
      }
    });
  }

  private push(quote: Quote): void {
    if (quote.last === null) {
      return;
    }
    const price = Number(quote.last);
    this.points.update((list) => [...list, { at: Date.now(), price }].slice(-MAX_POINTS));
  }
}
