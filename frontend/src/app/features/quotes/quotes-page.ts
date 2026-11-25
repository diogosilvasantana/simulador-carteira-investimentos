import { HttpClient } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  effect,
  inject,
  input,
  signal,
} from '@angular/core';
import {
  CdkFixedSizeVirtualScroll,
  CdkVirtualForOf,
  CdkVirtualScrollViewport,
} from '@angular/cdk/scrolling';
import { TypeCarousel, TypeTile } from '../../shared/type-carousel';
import { CATEGORIES, LISTING_LABELS, TYPE_LABELS, typeColor } from './categories';
import { formatChange, formatPrice, formatVolume } from './format';
import { Quote, QuoteRow, compareDecimal } from './quote';
import { SortKey, visibleRows } from './quote-list';
import { QuoteStore } from './quote-store';
import { QuoteStream } from './quote-stream';

interface CategoryCount {
  readonly slug: string;
  readonly count: number;
}

/** Altura fixa de cada linha: a rolagem virtual só desenha as linhas visíveis. */
export const ROW_HEIGHT = 56;

@Component({
  selector: 'app-quotes-page',
  imports: [
    DatePipe,
    TypeCarousel,
    CdkVirtualScrollViewport,
    CdkFixedSizeVirtualScroll,
    CdkVirtualForOf,
  ],
  providers: [QuoteStore],
  templateUrl: './quotes-page.html',
  styleUrl: './quotes-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class QuotesPage {
  private readonly http = inject(HttpClient);
  protected readonly store = inject(QuoteStore);
  protected readonly stream = inject(QuoteStream);

  /** Vem da rota /cotacoes/:categoria. */
  readonly categoria = input.required<string>();

  protected readonly categories = CATEGORIES;
  protected readonly rowHeight = ROW_HEIGHT;
  protected readonly counts = signal<Readonly<Record<string, number>>>({});
  protected readonly loading = signal(true);
  protected readonly failed = signal(false);
  protected readonly query = signal('');
  protected readonly sortKey = signal<SortKey>('ticker');
  protected readonly descending = signal(false);

  /** Carrossel de tipos: as categorias da bolsa e a renda fixa, que tem tela própria. */
  protected readonly tiles = computed<TypeTile[]>(() => {
    const counts = this.counts();
    const tiles: TypeTile[] = CATEGORIES.map((category) => ({
      id: category.slug,
      link: ['/cotacoes', category.slug],
      label: category.label,
      description: category.description,
      color: category.color,
      footnote: counts[category.slug] === undefined ? undefined : assetCount(counts[category.slug]),
    }));
    tiles.splice(1, 0, {
      id: 'renda-fixa',
      link: ['/renda-fixa'],
      label: 'Renda fixa',
      description: 'Tesouro, CDB, LCI e LCA com IR e IOF calculados.',
      color: 'var(--asset-renda-fixa)',
      footnote: 'CDI oficial do Banco Central',
    });
    return tiles;
  });

  protected readonly typeColor = typeColor;

  protected readonly current = computed(() =>
    CATEGORIES.find((category) => category.slug === this.categoria()),
  );

  protected readonly visible = computed(() =>
    visibleRows(this.store.rows(), {
      query: this.query(),
      sortKey: this.sortKey(),
      descending: this.descending(),
    }),
  );

  protected readonly formatPrice = formatPrice;
  protected readonly formatChange = formatChange;
  protected readonly formatVolume = formatVolume;

  constructor() {
    this.http.get<CategoryCount[]>('/api/categories').subscribe({
      next: (list) => this.counts.set(Object.fromEntries(list.map((c) => [c.slug, c.count]))),
    });

    effect((onCleanup) => {
      const category = this.current();
      if (!category) {
        return;
      }
      this.loading.set(true);
      this.failed.set(false);
      this.store.load([]);
      const follow = (): void =>
        this.stream.follow(`/topic/quotes/category/${category.slug}`, (batch) =>
          this.store.applyAll(batch),
        );
      // Assina depois do retrato, para um lote antigo não ser sobrescrito pelo carregamento.
      const request = this.http
        .get<Quote[]>('/api/assets', { params: { category: category.slug } })
        .subscribe({
          next: (quotes) => {
            this.store.load(quotes);
            this.loading.set(false);
            follow();
          },
          error: () => {
            this.loading.set(false);
            this.failed.set(true);
            follow();
          },
        });
      onCleanup(() => request.unsubscribe());
    });

    inject(DestroyRef).onDestroy(() => this.stream.stop());
  }

  protected sortBy(key: SortKey): void {
    if (this.sortKey() === key) {
      this.descending.update((value) => !value);
    } else {
      this.sortKey.set(key);
      // Variação e volume: o mais útil primeiro é o maior.
      this.descending.set(key !== 'ticker');
    }
  }

  protected ariaSort(key: SortKey): 'ascending' | 'descending' | 'none' {
    if (this.sortKey() !== key) {
      return 'none';
    }
    return this.descending() ? 'descending' : 'ascending';
  }

  protected trackByTicker(_: number, row: QuoteRow): string {
    return row.quote.ticker;
  }

  protected detail(quote: Quote): string {
    const type = quote.type ? TYPE_LABELS[quote.type] : '';
    const listing = quote.listingSegment ? LISTING_LABELS[quote.listingSegment] : '';
    return [type, listing].filter(Boolean).join(' · ') || '—';
  }

  protected changeTone(value: string | null): 'up' | 'down' | 'flat' {
    if (value === null) {
      return 'flat';
    }
    const diff = compareDecimal(value, '0');
    return diff > 0 ? 'up' : diff < 0 ? 'down' : 'flat';
  }

  protected countLabel(): string {
    const shown = this.visible().length;
    const total = this.store.rows().length;
    const noun = total === 1 ? 'ativo' : 'ativos';
    return shown === total ? `${total} ${noun}` : `${shown} de ${total} ${noun}`;
  }
}

function assetCount(count: number): string {
  return `${count.toLocaleString('pt-BR')} ${count === 1 ? 'ativo' : 'ativos'}`;
}
