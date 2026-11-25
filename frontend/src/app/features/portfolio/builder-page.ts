import { HttpClient } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Observable, catchError, concatMap, from, map, of, toArray } from 'rxjs';
import { AssetClassSlug, PortfolioError, PortfolioService } from '../../core/portfolio';
import { ASSET_CLASSES, AssetClassInfo } from '../../shared/asset-classes';
import { brl, parseMoney } from '../../shared/money';
import { FixedIncomeProduct } from '../fixed-income/fixed-income';
import { Quote, compareDecimal } from '../quotes/quote';
import { PROFILES, PlannedOrder, Profile, fromCents, planClass, toCents } from './builder';

type Step = 1 | 2 | 3 | 4;
type LineState = 'pending' | 'running' | 'ok' | 'error';

interface Candidate {
  readonly ticker: string;
  readonly name: string;
  readonly last: string;
  readonly volume: number;
}

interface Line {
  readonly label: string;
  readonly detail: string;
  readonly totalCents: number;
  readonly run: () => Observable<unknown>;
}

const SUGGESTIONS = 8;

/** Assistente de montar carteira: valor, divisão por tipo, escolha dos ativos e revisão. */
@Component({
  selector: 'app-builder-page',
  imports: [RouterLink],
  templateUrl: './builder-page.html',
  styleUrl: './builder-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BuilderPage {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  protected readonly portfolio = inject(PortfolioService);

  protected readonly classes = ASSET_CLASSES;
  protected readonly profiles = PROFILES;
  protected readonly brl = brl;
  protected readonly fromCents = fromCents;

  protected readonly step = signal<Step>(1);
  protected readonly amountText = signal('10.000,00');
  protected readonly amount = computed(() => parseMoney(this.amountText()));
  protected readonly profile = signal<Profile['id'] | null>('moderada');
  protected readonly weights = signal<Record<AssetClassSlug, number>>({ ...PROFILES[1].weights });
  protected readonly weightSum = computed(() =>
    Object.values(this.weights()).reduce((a, b) => a + b, 0),
  );

  protected readonly candidates = signal<Partial<Record<AssetClassSlug, Candidate[]>>>({});
  protected readonly chosen = signal<Partial<Record<AssetClassSlug, readonly string[]>>>({});
  protected readonly products = signal<readonly FixedIncomeProduct[]>([]);
  protected readonly product = signal<string | null>(null);
  protected readonly loadingAssets = signal(false);

  protected readonly states = signal<LineState[]>([]);
  protected readonly errors = signal<(string | null)[]>([]);
  protected readonly running = signal(false);
  protected readonly finished = signal(false);

  protected readonly activeClasses = computed(() =>
    ASSET_CLASSES.filter((c) => this.weights()[c.slug] > 0),
  );

  protected readonly budgetCents = computed(() => toCents(this.amount() ?? '0'));

  protected budgetOf(slug: AssetClassSlug): number {
    return Math.floor((this.budgetCents() * this.weights()[slug]) / 100);
  }

  /** As ordens que o assistente vai enviar, na ordem fixa das classes. */
  protected readonly plan = computed(() => {
    const orders: { info: AssetClassInfo; orders: PlannedOrder[] }[] = [];
    for (const info of this.activeClasses()) {
      if (info.slug === 'renda-fixa') {
        continue;
      }
      const picked = this.chosen()[info.slug] ?? [];
      const assets = (this.candidates()[info.slug] ?? []).filter((c) => picked.includes(c.ticker));
      orders.push({ info, orders: planClass(this.budgetOf(info.slug), assets) });
    }
    return orders;
  });

  protected readonly fixedIncomeCents = computed(() =>
    this.product() ? this.budgetOf('renda-fixa') : 0,
  );

  protected readonly plannedCents = computed(
    () =>
      this.plan().reduce((sum, g) => sum + g.orders.reduce((s, o) => s + o.totalCents, 0), 0) +
      this.fixedIncomeCents(),
  );

  /**
   * Aporte feito antes das ordens: completa o caixa até o valor que o usuário quer investir, para
   * que as sobras do plano fiquem mesmo no caixa. A margem de 0,1% cobre custos e preços que mudam.
   */
  protected readonly contributionCents = computed(() => {
    const cash = toCents(this.portfolio.summary()?.cash ?? '0');
    const needed = Math.max(this.budgetCents(), Math.ceil(this.plannedCents() * 1.001));
    return Math.max(0, needed - cash);
  });

  protected readonly lines = computed<Line[]>(() => {
    const lines: Line[] = [];
    const contribution = this.contributionCents();
    if (contribution > 0) {
      const amount = fromCents(contribution);
      lines.push({
        label: 'Aporte',
        detail: 'Dinheiro novo para cobrir o plano',
        totalCents: contribution,
        run: () => this.portfolio.contribute(amount),
      });
    }
    const productId = this.product();
    const fixed = this.fixedIncomeCents();
    if (productId && fixed > 0) {
      const name = this.products().find((p) => p.id === productId)?.name ?? productId;
      lines.push({
        label: name,
        detail: 'Aplicação em renda fixa',
        totalCents: fixed,
        run: () => this.portfolio.invest(productId, fromCents(fixed)),
      });
    }
    for (const group of this.plan()) {
      for (const order of group.orders) {
        lines.push({
          label: order.ticker,
          detail: `${order.quantity} × ${brl(fromCents(order.priceCents))} · ${group.info.label}`,
          totalCents: order.totalCents,
          run: () =>
            this.portfolio.order({ ticker: order.ticker, side: 'BUY', quantity: order.quantity }),
        });
      }
    }
    return lines;
  });

  constructor() {
    this.portfolio.load();
    this.http.get<FixedIncomeProduct[]>('/api/fixed-income/products').subscribe({
      next: (list) => {
        this.products.set(list);
        // Começa pelo melhor CDB de liquidez livre: sem carência, quem treina pode resgatar logo.
        const first = list.find((p) => p.gracePeriodDays === 0 && p.kind === 'CDB') ?? list[0];
        this.product.set(first?.id ?? null);
      },
      error: () => this.products.set([]),
    });
  }

  protected applyProfile(profile: Profile): void {
    this.profile.set(profile.id);
    this.weights.set({ ...profile.weights });
  }

  protected setWeight(slug: AssetClassSlug, value: string): void {
    const parsed = Math.max(0, Math.min(100, Math.round(Number(value) || 0)));
    this.profile.set(null);
    this.weights.update((w) => ({ ...w, [slug]: parsed }));
  }

  protected goToAssets(): void {
    this.step.set(3);
    const needed = this.activeClasses().filter(
      (c) => c.slug !== 'renda-fixa' && !this.candidates()[c.slug],
    );
    if (needed.length === 0) {
      return;
    }
    this.loadingAssets.set(true);
    from(needed)
      .pipe(
        concatMap((info) =>
          this.http
            .get<Quote[]>('/api/assets', { params: { category: info.slug } })
            .pipe(map((quotes) => ({ info, quotes }))),
        ),
        toArray(),
      )
      .subscribe({
        next: (results) => {
          const candidates = { ...this.candidates() };
          const chosen = { ...this.chosen() };
          for (const { info, quotes } of results) {
            // Sugestões: os mais negociados, que têm preço confiável e liquidez para treinar.
            const list = quotes
              .filter((q) => q.last !== null && compareDecimal(q.last, '0') > 0)
              .sort((a, b) => (b.volume ?? 0) - (a.volume ?? 0))
              .slice(0, SUGGESTIONS)
              .map((q) => ({
                ticker: q.ticker,
                name: q.name ?? q.ticker,
                last: q.last!,
                volume: q.volume ?? 0,
              }));
            candidates[info.slug] = list;
            chosen[info.slug] ??= list.slice(0, 2).map((c) => c.ticker);
          }
          this.candidates.set(candidates);
          this.chosen.set(chosen);
          this.loadingAssets.set(false);
        },
        error: () => this.loadingAssets.set(false),
      });
  }

  protected toggle(slug: AssetClassSlug, ticker: string): void {
    this.chosen.update((c) => {
      const list = c[slug] ?? [];
      return {
        ...c,
        [slug]: list.includes(ticker) ? list.filter((t) => t !== ticker) : [...list, ticker],
      };
    });
  }

  protected isChosen(slug: AssetClassSlug, ticker: string): boolean {
    return (this.chosen()[slug] ?? []).includes(ticker);
  }

  protected execute(): void {
    const lines = this.lines();
    this.running.set(true);
    this.states.set(lines.map(() => 'pending'));
    this.errors.set(lines.map(() => null));
    from(lines.map((line, index) => ({ line, index })))
      .pipe(
        concatMap(({ line, index }) => {
          this.setState(index, 'running');
          return line.run().pipe(
            map(() => this.setState(index, 'ok')),
            // Uma ordem que falha não impede as outras; o erro fica na linha.
            catchError((error: unknown) => {
              const message = error instanceof PortfolioError ? error.message : 'Falhou.';
              this.setState(index, 'error');
              this.errors.update((list) => list.map((m, i) => (i === index ? message : m)));
              return of(null);
            }),
          );
        }),
        toArray(),
      )
      .subscribe(() => {
        this.running.set(false);
        this.finished.set(true);
        this.portfolio.load();
      });
  }

  protected goToPortfolio(): void {
    void this.router.navigate(['/carteira']);
  }

  protected value(event: Event): string {
    return (event.target as HTMLInputElement).value;
  }

  private setState(index: number, state: LineState): void {
    this.states.update((list) => list.map((s, i) => (i === index ? state : s)));
  }
}
