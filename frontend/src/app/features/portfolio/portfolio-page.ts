import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PortfolioError, PortfolioService, PositionView } from '../../core/portfolio';
import { ASSET_CLASSES, AssetClassInfo } from '../../shared/asset-classes';
import { brl, parseMoney, percent, signedBrl, tone } from '../../shared/money';
import { subtract } from '../fixed-income/fixed-income';
import { TradeService } from '../../shared/trade';

interface Group {
  readonly info: AssetClassInfo;
  readonly positions: readonly PositionView[];
  readonly value: string;
  readonly weight: string;
}

@Component({
  selector: 'app-portfolio-page',
  imports: [RouterLink, DatePipe],
  templateUrl: './portfolio-page.html',
  styleUrl: './portfolio-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PortfolioPage {
  protected readonly portfolio = inject(PortfolioService);
  protected readonly trade = inject(TradeService);

  protected readonly brl = brl;
  protected readonly signedBrl = signedBrl;
  protected readonly percent = percent;
  protected readonly tone = tone;
  protected readonly subtract = subtract;

  protected readonly amount = signal('5.000,00');
  protected readonly contributing = signal(false);
  protected readonly message = signal<string | null>(null);
  protected readonly confirmReset = signal(false);

  protected readonly summary = this.portfolio.summary;
  protected readonly empty = computed(() => {
    const s = this.summary();
    return s !== null && s.contributed === '0.00' && s.positions.length === 0;
  });

  /** Alocação na ordem fixa das classes, só com o que a carteira tem. */
  protected readonly allocation = computed(() => {
    const list = this.summary()?.allocation ?? [];
    return ASSET_CLASSES.map((info) => ({
      info,
      entry: list.find((a) => a.assetClass === info.slug),
    })).filter((a) => a.entry !== undefined);
  });

  protected readonly groups = computed<Group[]>(() => {
    const s = this.summary();
    if (!s) {
      return [];
    }
    return ASSET_CLASSES.filter((info) => info.slug !== 'renda-fixa')
      .map((info) => {
        const entry = s.allocation.find((a) => a.assetClass === info.slug);
        return {
          info,
          positions: s.positions.filter((p) => p.assetClass === info.slug),
          value: entry?.value ?? '0',
          weight: entry?.percent ?? '0',
        };
      })
      .filter((g) => g.positions.length > 0);
  });

  protected readonly fixedWeight = computed(
    () => this.summary()?.allocation.find((a) => a.assetClass === 'renda-fixa')?.percent ?? '0',
  );

  constructor() {
    this.portfolio.load();
  }

  protected contribute(): void {
    const value = parseMoney(this.amount());
    if (!value) {
      this.message.set('Informe um valor positivo, como 5.000,00.');
      return;
    }
    this.contributing.set(true);
    this.message.set(null);
    this.portfolio.contribute(value).subscribe({
      next: () => {
        this.contributing.set(false);
        this.message.set(`Aporte de ${brl(value)} feito. O dinheiro está no caixa.`);
      },
      error: (e: PortfolioError) => {
        this.contributing.set(false);
        this.message.set(e.message);
      },
    });
  }

  protected reset(): void {
    if (!this.confirmReset()) {
      this.confirmReset.set(true);
      return;
    }
    this.portfolio.reset().subscribe(() => this.confirmReset.set(false));
  }

  protected value(event: Event): string {
    return (event.target as HTMLInputElement).value;
  }
}
