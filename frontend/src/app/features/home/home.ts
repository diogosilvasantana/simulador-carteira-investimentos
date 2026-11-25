import { HttpClient } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TypeCarousel, TypeTile } from '../../shared/type-carousel';
import { CATEGORIES } from '../quotes/categories';

interface Phase {
  readonly name: string;
  readonly description: string;
  readonly ready: boolean;
}

interface CategoryCount {
  readonly slug: string;
  readonly count: number;
}

@Component({
  selector: 'app-home',
  imports: [RouterLink, TypeCarousel],
  templateUrl: './home.html',
  styleUrl: './home.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Home {
  private readonly counts = signal<Readonly<Record<string, number>>>({});

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

  protected readonly phases = signal<readonly Phase[]>([
    {
      name: 'Cotações ao vivo',
      description: 'Todos os ativos da B3 pelo MT5, por tipo',
      ready: true,
    },
    {
      name: 'Renda fixa',
      description: 'CDI oficial, IR e IOF, comparação pelo líquido',
      ready: true,
    },
    {
      name: 'Carteira e ordens',
      description: 'Aportes, compra e venda, posição e preço médio',
      ready: false,
    },
    { name: 'Proventos', description: 'Dividendos, JCP e reinvestimento automático', ready: false },
    {
      name: 'Rentabilidade e rebalanceamento',
      description: 'Carteira contra CDI e Ibovespa, alocação-alvo',
      ready: false,
    },
  ]);

  constructor() {
    inject(HttpClient)
      .get<CategoryCount[]>('/api/categories')
      .subscribe({
        next: (list) => this.counts.set(Object.fromEntries(list.map((c) => [c.slug, c.count]))),
        // Sem backend, o carrossel funciona sem as contagens.
        error: () => undefined,
      });
  }
}
