import { visibleRows } from './quote-list';
import { aRow } from './testing';

describe('visibleRows', () => {
  const rows = [
    aRow({ ticker: 'ABEV3', name: 'AMBEV S/A', changePercent: '-2.10', volume: 500 }),
    aRow({ ticker: 'PETR4', name: 'PETROBRAS', changePercent: '3.97', volume: 900 }),
    aRow({ ticker: 'AAAA3', name: 'SEM NEGOCIO', changePercent: null, last: null }),
    aRow({ ticker: 'VALE3', name: 'VALE', changePercent: '0.50', volume: 700 }),
  ];
  const tickers = (list: ReturnType<typeof visibleRows>): string[] =>
    list.map((row) => row.quote.ticker);

  it('should sort by ticker', () => {
    const result = visibleRows(rows, { query: '', sortKey: 'ticker', descending: false });

    expect(tickers(result)).toEqual(['AAAA3', 'ABEV3', 'PETR4', 'VALE3']);
  });

  it('should sort by change with the biggest gain first and unknown change last', () => {
    const result = visibleRows(rows, { query: '', sortKey: 'change', descending: true });

    expect(tickers(result)).toEqual(['PETR4', 'VALE3', 'ABEV3', 'AAAA3']);
  });

  it('should keep assets without trades last when sorting by volume', () => {
    const result = visibleRows(rows, { query: '', sortKey: 'volume', descending: false });

    expect(tickers(result)).toEqual(['ABEV3', 'VALE3', 'PETR4', 'AAAA3']);
  });

  it('should search by ticker or by name', () => {
    expect(
      tickers(visibleRows(rows, { query: 'pet', sortKey: 'ticker', descending: false })),
    ).toEqual(['PETR4']);
    expect(
      tickers(visibleRows(rows, { query: 'ambev', sortKey: 'ticker', descending: false })),
    ).toEqual(['ABEV3']);
  });
});
