import { HttpClient } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { Quote, QuoteRow, compareDecimal } from './quote';
import { QuoteStore } from './quote-store';
import { QuoteStream } from './quote-stream';

export type SortKey = 'ticker' | 'change';

@Component({
  selector: 'app-quotes-page',
  imports: [DatePipe],
  templateUrl: './quotes-page.html',
  styleUrl: './quotes-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class QuotesPage implements OnInit {
  private readonly http = inject(HttpClient);
  protected readonly store = inject(QuoteStore);
  protected readonly stream = inject(QuoteStream);

  protected readonly query = signal('');
  protected readonly sortKey = signal<SortKey>('ticker');
  protected readonly descending = signal(false);

  protected readonly visibleRows = computed(() => {
    const query = this.query().trim().toUpperCase();
    const rows = query
      ? this.store.rows().filter((row) => row.quote.ticker.includes(query))
      : this.store.rows();
    const sign = this.descending() ? -1 : 1;
    if (this.sortKey() === 'ticker') {
      return [...rows].sort((a, b) => sign * byTicker(a, b));
    }
    // Ativos sem variação conhecida ficam no fim, qualquer que seja a direção.
    const known = rows.filter((row) => row.quote.changePercent !== null);
    const unknown = rows.filter((row) => row.quote.changePercent === null);
    return [...known.sort((a, b) => sign * byChange(a, b)), ...unknown.sort(byTicker)];
  });

  protected readonly stateLabel = {
    connected: 'Ao vivo',
    connecting: 'Conectando…',
    disconnected: 'Desconectado',
  } as const;

  protected readonly sourceLabel = {
    LIVE: 'MT5',
    REPLAY: 'Replay',
    SYNTHETIC: 'Sintético',
  } as const;

  ngOnInit(): void {
    const onBatch = (quotes: Quote[]): void => this.store.applyAll(quotes);
    // Carrega o retrato atual antes de assinar, para a tabela não começar vazia.
    this.http.get<Quote[]>('/api/quotes').subscribe({
      next: (quotes) => {
        this.store.load(quotes);
        this.stream.connect(onBatch);
      },
      error: () => this.stream.connect(onBatch),
    });
  }

  protected sortBy(key: SortKey): void {
    if (this.sortKey() === key) {
      this.descending.update((value) => !value);
    } else {
      this.sortKey.set(key);
      // Na variação, o mais útil primeiro é quem mais subiu.
      this.descending.set(key === 'change');
    }
  }

  protected ariaSort(key: SortKey): 'ascending' | 'descending' | 'none' {
    if (this.sortKey() !== key) {
      return 'none';
    }
    return this.descending() ? 'descending' : 'ascending';
  }

  protected formatPrice(value: string | null): string {
    if (value === null) {
      return '—';
    }
    const negative = value.startsWith('-');
    const [integer, fraction = ''] = (negative ? value.slice(1) : value).split('.');
    const grouped = integer.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
    return `${negative ? '-' : ''}${grouped},${fraction.padEnd(2, '0')}`;
  }

  protected formatChange(value: string | null): string {
    if (value === null) {
      return '—';
    }
    const sign = compareDecimal(value, '0') > 0 ? '+' : '';
    return `${sign}${this.formatPrice(value)}%`;
  }

  protected changeClass(value: string | null): string {
    if (value === null) {
      return 'num change';
    }
    const diff = compareDecimal(value, '0');
    return diff > 0 ? 'num change change--up' : diff < 0 ? 'num change change--down' : 'num change';
  }
}

function byTicker(a: QuoteRow, b: QuoteRow): number {
  return a.quote.ticker.localeCompare(b.quote.ticker);
}

function byChange(a: QuoteRow, b: QuoteRow): number {
  return compareDecimal(a.quote.changePercent ?? '0', b.quote.changePercent ?? '0');
}
