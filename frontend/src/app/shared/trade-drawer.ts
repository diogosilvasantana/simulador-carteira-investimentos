import { HttpClient } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  signal,
  untracked,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  ExecutionView,
  OrderQuote,
  OrderSide,
  PortfolioError,
  PortfolioService,
} from '../core/portfolio';
import { typeColor } from '../features/quotes/categories';
import { Quote, compareDecimal } from '../features/quotes/quote';
import { Icon } from './icon';
import { brl, percent, tone } from './money';
import { TradeService } from './trade';

type Step = 'form' | 'review' | 'done';

/** A boleta: escolhe lado e quantidade, revisa com os custos e só então envia. */
@Component({
  selector: 'app-trade-drawer',
  imports: [RouterLink, Icon],
  templateUrl: './trade-drawer.html',
  styleUrl: './trade-drawer.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { '(document:keydown.escape)': 'close()' },
})
export class TradeDrawer {
  private readonly http = inject(HttpClient);
  protected readonly trade = inject(TradeService);
  protected readonly portfolio = inject(PortfolioService);

  protected readonly brl = brl;
  protected readonly percent = percent;
  protected readonly tone = tone;
  protected readonly typeColor = typeColor;

  protected readonly quote = signal<Quote | null>(null);
  protected readonly side = signal<OrderSide>('BUY');
  protected readonly quantity = signal(1);
  protected readonly step = signal<Step>('form');
  protected readonly review = signal<OrderQuote | null>(null);
  protected readonly done = signal<ExecutionView | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly busy = signal(false);

  protected readonly held = computed(() => {
    const ticker = this.trade.current()?.ticker;
    return this.portfolio.summary()?.positions.find((p) => p.ticker === ticker)?.quantity ?? 0;
  });

  /** Estimativa sem custos, só para orientar; o valor certo vem da revisão. */
  protected readonly estimate = computed(() => {
    const last = this.quote()?.last;
    if (!last) {
      return null;
    }
    return (Math.round(Number(last) * 100) * this.quantity()) / 100;
  });

  protected readonly overCash = computed(() => {
    const estimate = this.estimate();
    const cash = this.portfolio.summary()?.cash;
    return (
      this.side() === 'BUY' &&
      estimate !== null &&
      cash !== undefined &&
      compareDecimal(estimate.toFixed(2), cash) > 0
    );
  });

  protected readonly overPosition = computed(
    () => this.side() === 'SELL' && this.quantity() > this.held(),
  );

  constructor() {
    effect(() => {
      const request = this.trade.current();
      if (!request) {
        return;
      }
      untracked(() => {
        this.side.set(request.side);
        this.quantity.set(request.side === 'SELL' ? Math.max(1, this.held()) : 1);
        this.step.set('form');
        this.error.set(null);
        this.quote.set(null);
        this.portfolio.load();
        this.http.get<Quote>(`/api/assets/${request.ticker}`).subscribe({
          next: (quote) => this.quote.set(quote),
          error: () => this.error.set('Não encontramos esse ativo no catálogo.'),
        });
      });
    });
  }

  protected setQuantity(value: string | number): void {
    const parsed = Math.floor(Number(value));
    this.quantity.set(Number.isFinite(parsed) && parsed > 0 ? parsed : 1);
  }

  protected step1(delta: number): void {
    this.quantity.update((q) => Math.max(1, q + delta));
  }

  protected toReview(): void {
    const request = this.trade.current();
    if (!request) {
      return;
    }
    this.busy.set(true);
    this.error.set(null);
    this.portfolio
      .preview({ ticker: request.ticker, side: this.side(), quantity: this.quantity() })
      .subscribe({
        next: (quote) => {
          this.review.set(quote);
          this.step.set('review');
          this.busy.set(false);
        },
        error: (e: PortfolioError) => this.fail(e),
      });
  }

  protected confirm(): void {
    const request = this.trade.current();
    if (!request) {
      return;
    }
    this.busy.set(true);
    this.error.set(null);
    this.portfolio
      .order({ ticker: request.ticker, side: this.side(), quantity: this.quantity() })
      .subscribe({
        next: (execution) => {
          this.done.set(execution);
          this.step.set('done');
          this.busy.set(false);
        },
        error: (e: PortfolioError) => this.fail(e),
      });
  }

  protected close(): void {
    this.trade.close();
  }

  private fail(error: PortfolioError): void {
    this.error.set(error.message);
    this.busy.set(false);
  }
}
