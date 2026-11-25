import { DatePipe } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { formatPrice } from '../quotes/format';
import {
  FixedIncomeProduct,
  INDEX_LABELS,
  IndexRate,
  KIND_LABELS,
  ProductResult,
  Simulation,
  isoDate,
  parseAmount,
  rankByNet,
  requestFor,
  subtract,
} from './fixed-income';

const API = '/api/fixed-income';

@Component({
  selector: 'app-fixed-income-page',
  imports: [DatePipe],
  templateUrl: './fixed-income-page.html',
  styleUrl: './fixed-income-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FixedIncomePage {
  private readonly http = inject(HttpClient);

  protected readonly indexLabels = INDEX_LABELS;
  protected readonly kindLabels = KIND_LABELS;
  protected readonly money = formatPrice;

  protected readonly indexes = signal<readonly IndexRate[] | null>(null);
  protected readonly products = signal<readonly FixedIncomeProduct[]>([]);
  protected readonly results = signal<readonly ProductResult[]>([]);
  protected readonly simulating = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly amount = signal('10.000,00');
  protected readonly start = signal(isoDate(new Date()));
  protected readonly end = signal(isoDate(oneYearFromNow()));

  protected readonly principal = computed(() => parseAmount(this.amount()));
  protected readonly validPeriod = computed(() => this.end() > this.start());
  protected readonly projected = computed(() =>
    this.results().some((r) => r.simulation.projectedBusinessDays > 0),
  );

  constructor() {
    this.http.get<IndexRate[]>(`${API}/indexes`).subscribe({
      next: (rates) => this.indexes.set(rates),
      error: () => this.indexes.set([]),
    });
    this.http.get<FixedIncomeProduct[]>(`${API}/products`).subscribe({
      next: (products) => {
        this.products.set(products);
        this.simulate();
      },
      error: () => this.error.set('Não foi possível carregar os produtos. O backend está no ar?'),
    });
  }

  protected simulate(): void {
    const principal = this.principal();
    const products = this.products();
    if (principal === null || !this.validPeriod() || products.length === 0) {
      return;
    }
    const [start, end] = [this.start(), this.end()];
    this.simulating.set(true);
    this.error.set(null);
    forkJoin(
      products.map((product) =>
        this.http.post<Simulation>(
          `${API}/simulations`,
          requestFor(product, principal, start, end),
        ),
      ),
    ).subscribe({
      next: (simulations) => {
        this.results.set(
          rankByNet(
            simulations.map((simulation, i) => ({
              product: products[i],
              simulation,
              blockedByGracePeriod: simulation.holdingDays < products[i].gracePeriodDays,
            })),
          ),
        );
        this.simulating.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.results.set([]);
        this.simulating.set(false);
        this.error.set(
          error.status === 503
            ? 'Os indexadores ainda estão chegando do Banco Central. Tente de novo em instantes.'
            : 'Não foi possível simular. Confira o valor e as datas.',
        );
      },
    });
  }

  protected netYield(row: ProductResult): string {
    return subtract(row.simulation.net, row.simulation.principal);
  }

  protected value(event: Event): string {
    return (event.target as HTMLInputElement).value;
  }
}

function oneYearFromNow(): Date {
  const date = new Date();
  date.setFullYear(date.getFullYear() + 1);
  return date;
}
