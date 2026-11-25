import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { brl, parseMoney } from '../../shared/money';
import { formatPrice } from '../quotes/format';
import {
  BudgetKind,
  KIND_LABELS,
  STAGES,
  Snapshot,
  isNegative,
  optionalMoney,
  optionalRate,
} from './today';

const API = '/api/personal-finance';

@Component({
  selector: 'app-today-page',
  templateUrl: './today-page.html',
  styleUrl: './today-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TodayPage {
  private readonly http = inject(HttpClient);

  protected readonly brl = brl;
  protected readonly rate = formatPrice;
  protected readonly isNegative = isNegative;
  protected readonly kindLabels = KIND_LABELS;
  protected readonly kinds = Object.keys(KIND_LABELS) as BudgetKind[];

  protected readonly snapshot = signal<Snapshot | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly saving = signal(false);
  /** Remover pede um segundo clique: a chave do registro que espera confirmação. */
  protected readonly confirming = signal<string | null>(null);

  protected readonly itemName = signal('');
  protected readonly itemKind = signal<BudgetKind>('INCOME');
  protected readonly itemAmount = signal('');

  protected readonly creditor = signal('');
  protected readonly balance = signal('');
  protected readonly monthlyRate = signal('');
  protected readonly installment = signal('');
  protected readonly overdue = signal(false);
  protected readonly collateral = signal(false);

  protected readonly stage = computed(() => STAGES[this.snapshot()?.stage ?? 'NEEDS_DATA']);
  protected readonly top = computed(() => this.snapshot()?.debts.slice(0, 3) ?? []);
  protected readonly validItem = computed(
    () => this.itemName().trim() !== '' && parseMoney(this.itemAmount()) !== null,
  );
  protected readonly validDebt = computed(
    () =>
      this.creditor().trim() !== '' &&
      parseMoney(this.balance()) !== null &&
      optionalRate(this.monthlyRate()) !== undefined &&
      optionalMoney(this.installment()) !== undefined,
  );

  constructor() {
    this.load();
  }

  protected addItem(): void {
    if (!this.validItem()) {
      return;
    }
    const body = {
      name: this.itemName().trim(),
      kind: this.itemKind(),
      amount: parseMoney(this.itemAmount()),
    };
    this.run(this.http.post(`${API}/budget-items`, body), () => {
      this.itemName.set('');
      this.itemAmount.set('');
    });
  }

  protected addDebt(): void {
    if (!this.validDebt()) {
      return;
    }
    const body = {
      creditor: this.creditor().trim(),
      balance: parseMoney(this.balance()),
      monthlyRatePercent: optionalRate(this.monthlyRate()),
      installment: optionalMoney(this.installment()),
      overdue: this.overdue(),
      collateral: this.collateral(),
    };
    this.run(this.http.post(`${API}/debts`, body), () => {
      for (const field of [this.creditor, this.balance, this.monthlyRate, this.installment]) {
        field.set('');
      }
      this.overdue.set(false);
      this.collateral.set(false);
    });
  }

  protected remove(path: 'budget-items' | 'debts', id: number): void {
    const key = `${path}/${id}`;
    if (this.confirming() !== key) {
      this.confirming.set(key);
      return;
    }
    this.run(this.http.delete(`${API}/${key}`), () => this.confirming.set(null));
  }

  protected value(event: Event): string {
    return (event.target as HTMLInputElement | HTMLSelectElement).value;
  }

  protected kind(event: Event): BudgetKind {
    return this.value(event) as BudgetKind;
  }

  protected checked(event: Event): boolean {
    return (event.target as HTMLInputElement).checked;
  }

  private load(): void {
    this.http.get<Snapshot>(API).subscribe({
      next: (snapshot) => {
        this.snapshot.set(snapshot);
        this.error.set(null);
      },
      error: (error: HttpErrorResponse) =>
        this.error.set(
          error.status === 404
            ? 'As finanças pessoais só funcionam no modo local, até existir login.'
            : 'O backend não respondeu. Ele está rodando?',
        ),
    });
  }

  private run(request: Observable<unknown>, done: () => void): void {
    this.saving.set(true);
    request.subscribe({
      next: () => {
        this.saving.set(false);
        done();
        this.load();
      },
      error: () => {
        this.saving.set(false);
        this.error.set('Não foi possível salvar. Confira os valores e tente de novo.');
      },
    });
  }
}
