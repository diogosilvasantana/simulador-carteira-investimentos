import { Injectable, computed, signal } from '@angular/core';
import { Direction, Quote, QuoteRow, compareDecimal } from './quote';

/** Última cotação de cada ativo, com a direção em relação ao tick anterior. */
@Injectable({ providedIn: 'root' })
export class QuoteStore {
  private readonly byTicker = signal<ReadonlyMap<string, QuoteRow>>(new Map());

  readonly rows = computed(() =>
    [...this.byTicker().values()].sort((a, b) => a.quote.ticker.localeCompare(b.quote.ticker)),
  );

  load(quotes: readonly Quote[]): void {
    const map = new Map<string, QuoteRow>();
    for (const quote of quotes) {
      map.set(quote.ticker, { quote, direction: 'flat', revision: 0 });
    }
    this.byTicker.set(map);
  }

  apply(quote: Quote): void {
    this.applyAll([quote]);
  }

  /** Aplica um lote de uma vez: com centenas de ativos, copiar o mapa a cada tick pesa. */
  applyAll(quotes: readonly Quote[]): void {
    if (quotes.length === 0) {
      return;
    }
    this.byTicker.update((current) => {
      const next = new Map(current);
      for (const quote of quotes) {
        const previous = next.get(quote.ticker);
        // Ticks podem chegar fora de ordem na reconexão; o mais antigo é descartado.
        if (previous && quote.occurredAt < previous.quote.occurredAt) {
          continue;
        }
        next.set(quote.ticker, {
          quote,
          direction: previous ? directionOf(previous.quote.last, quote.last) : 'flat',
          revision: (previous?.revision ?? 0) + 1,
        });
      }
      return next;
    });
  }
}

function directionOf(before: string, after: string): Direction {
  const diff = compareDecimal(after, before);
  return diff > 0 ? 'up' : diff < 0 ? 'down' : 'flat';
}
