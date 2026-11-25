import { Injectable, computed, signal } from '@angular/core';
import { Direction, Quote, QuoteRow, compareDecimal } from './quote';

/** Ativos da categoria aberta, com a direção do último movimento de preço. */
@Injectable()
export class QuoteStore {
  private readonly byTicker = signal<ReadonlyMap<string, QuoteRow>>(new Map());

  readonly rows = computed(() => [...this.byTicker().values()]);

  load(quotes: readonly Quote[]): void {
    const map = new Map<string, QuoteRow>();
    for (const quote of quotes) {
      map.set(quote.ticker, { quote, direction: 'flat', revision: 0 });
    }
    this.byTicker.set(map);
  }

  /** Aplica um lote de uma vez: o backend já manda só a última cotação de cada ativo. */
  applyAll(quotes: readonly Quote[]): void {
    if (quotes.length === 0) {
      return;
    }
    this.byTicker.update((current) => {
      const next = new Map(current);
      for (const quote of quotes) {
        const previous = next.get(quote.ticker);
        // Ticks podem chegar fora de ordem na reconexão; o mais antigo é descartado.
        if (isOlder(quote, previous?.quote)) {
          continue;
        }
        next.set(quote.ticker, {
          quote,
          direction: directionOf(previous?.quote.last ?? null, quote.last),
          revision: (previous?.revision ?? 0) + 1,
        });
      }
      return next;
    });
  }
}

function isOlder(quote: Quote, current: Quote | undefined): boolean {
  if (!current?.occurredAt || !quote.occurredAt) {
    return false;
  }
  return quote.occurredAt < current.occurredAt;
}

function directionOf(before: string | null, after: string | null): Direction {
  if (before === null || after === null) {
    return 'flat';
  }
  const diff = compareDecimal(after, before);
  return diff > 0 ? 'up' : diff < 0 ? 'down' : 'flat';
}
