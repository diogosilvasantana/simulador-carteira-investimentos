import { QuoteRow, compareDecimal } from './quote';

export type SortKey = 'ticker' | 'change' | 'volume';

export interface ListView {
  readonly query: string;
  readonly sortKey: SortKey;
  readonly descending: boolean;
}

/**
 * Filtra e ordena a lista da categoria. Ativos sem o dado da ordenação (sem variação ou sem
 * negócio hoje) ficam sempre no fim, qualquer que seja a direção.
 */
export function visibleRows(rows: readonly QuoteRow[], view: ListView): QuoteRow[] {
  const query = view.query.trim().toUpperCase();
  const filtered = query
    ? rows.filter(
        (row) =>
          row.quote.ticker.includes(query) || (row.quote.name ?? '').toUpperCase().includes(query),
      )
    : rows;
  const sign = view.descending ? -1 : 1;
  if (view.sortKey === 'ticker') {
    return [...filtered].sort((a, b) => sign * byTicker(a, b));
  }
  const valueOf = view.sortKey === 'change' ? changeOf : volumeOf;
  const known = filtered.filter((row) => valueOf(row) !== null);
  const unknown = filtered.filter((row) => valueOf(row) === null);
  const compare = view.sortKey === 'change' ? byChange : byVolume;
  return [...known.sort((a, b) => sign * compare(a, b)), ...unknown.sort(byTicker)];
}

function changeOf(row: QuoteRow): string | null {
  return row.quote.changePercent;
}

function volumeOf(row: QuoteRow): number | null {
  return row.quote.last === null ? null : row.quote.volume;
}

function byTicker(a: QuoteRow, b: QuoteRow): number {
  return a.quote.ticker.localeCompare(b.quote.ticker);
}

function byChange(a: QuoteRow, b: QuoteRow): number {
  return compareDecimal(a.quote.changePercent ?? '0', b.quote.changePercent ?? '0');
}

function byVolume(a: QuoteRow, b: QuoteRow): number {
  return (a.quote.volume ?? 0) - (b.quote.volume ?? 0);
}
