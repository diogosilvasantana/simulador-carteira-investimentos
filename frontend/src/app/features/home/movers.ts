import { Quote, compareDecimal } from '../quotes/quote';

export interface Movers {
  readonly gainers: readonly Quote[];
  readonly losers: readonly Quote[];
  readonly active: readonly Quote[];
}

/** Maiores altas, maiores baixas e mais negociados do dia, entre os ativos que já negociaram. */
export function movers(quotes: readonly Quote[], size = 5): Movers {
  const traded = quotes.filter(
    (q): q is Quote & { changePercent: string } => q.last !== null && q.changePercent !== null,
  );
  const byChange = [...traded].sort((a, b) => compareDecimal(b.changePercent, a.changePercent));
  return {
    gainers: byChange.filter((q) => compareDecimal(q.changePercent, '0') > 0).slice(0, size),
    losers: byChange
      .filter((q) => compareDecimal(q.changePercent, '0') < 0)
      .reverse()
      .slice(0, size),
    active: [...traded].sort((a, b) => (b.volume ?? 0) - (a.volume ?? 0)).slice(0, size),
  };
}
