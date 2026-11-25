import { HttpClient } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PortfolioService } from '../../core/portfolio';
import { ASSET_CLASSES } from '../../shared/asset-classes';
import { brl, percent, signedBrl, tone } from '../../shared/money';
import { TypeCarousel, TypeTile } from '../../shared/type-carousel';
import { CATEGORIES, typeColor } from '../quotes/categories';
import { formatVolume } from '../quotes/format';
import { Quote, compareDecimal } from '../quotes/quote';
import { Movers, movers } from './movers';

interface CategoryCount {
  readonly slug: string;
  readonly count: number;
}

interface MoverGroup {
  readonly id: keyof Movers;
  readonly title: string;
  readonly empty: string;
}

@Component({
  selector: 'app-home',
  imports: [RouterLink, TypeCarousel],
  templateUrl: './home.html',
  styleUrl: './home.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Home {
  protected readonly portfolio = inject(PortfolioService);

  protected readonly brl = brl;
  protected readonly signedBrl = signedBrl;
  protected readonly percent = percent;
  protected readonly tone = tone;
  protected readonly typeColor = typeColor;
  protected readonly formatVolume = formatVolume;

  private readonly counts = signal<Readonly<Record<string, number>>>({});
  private readonly quotes = signal<readonly Quote[] | null>(null);

  protected readonly groups: readonly MoverGroup[] = [
    { id: 'gainers', title: 'Maiores altas', empty: 'Nenhum ativo em alta ainda.' },
    { id: 'losers', title: 'Maiores baixas', empty: 'Nenhum ativo em baixa ainda.' },
    { id: 'active', title: 'Mais negociados', empty: 'Ainda não houve negócios.' },
  ];
  protected readonly market = computed(() => {
    const list = this.quotes();
    return list === null ? null : movers(list);
  });

  /** Carteira com algum aporte: a Início vira um resumo dela em vez do convite para começar. */
  protected readonly started = computed(() => {
    const s = this.portfolio.summary();
    return s !== null && compareDecimal(s.contributed, '0') > 0;
  });
  protected readonly allocation = computed(() => {
    const list = this.portfolio.summary()?.allocation ?? [];
    return ASSET_CLASSES.flatMap((info) => {
      const entry = list.find((a) => a.assetClass === info.slug);
      return entry ? [{ info, entry }] : [];
    });
  });

  protected readonly tiles = computed<TypeTile[]>(() => {
    const counts = this.counts();
    const tiles: TypeTile[] = CATEGORIES.map((category) => ({
      id: category.slug,
      link: ['/cotacoes', category.slug],
      label: category.label,
      description: category.description,
      color: category.color,
      footnote:
        counts[category.slug] === undefined
          ? undefined
          : `${counts[category.slug].toLocaleString('pt-BR')} ativos`,
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

  constructor() {
    const http = inject(HttpClient);
    this.portfolio.load();
    http.get<CategoryCount[]>('/api/categories').subscribe({
      next: (list) => this.counts.set(Object.fromEntries(list.map((c) => [c.slug, c.count]))),
      // Sem backend, o carrossel funciona sem as contagens.
      error: () => undefined,
    });
    http.get<Quote[]>('/api/quotes').subscribe({
      next: (list) => this.quotes.set(list),
      error: () => this.quotes.set([]),
    });
  }
}
