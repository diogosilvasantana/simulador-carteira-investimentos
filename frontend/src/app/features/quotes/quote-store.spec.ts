import { TestBed } from '@angular/core/testing';
import { compareDecimal } from './quote';
import { QuoteStore } from './quote-store';
import { aQuote } from './testing';

describe('QuoteStore', () => {
  let store: QuoteStore;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [QuoteStore] });
    store = TestBed.inject(QuoteStore);
  });

  it('should mark a higher price as up and a lower one as down', () => {
    store.load([aQuote({ ticker: 'PETR4', last: '38.50', occurredAt: '2026-11-24T13:00:00Z' })]);

    store.applyAll([
      aQuote({ ticker: 'PETR4', last: '38.51', occurredAt: '2026-11-24T13:00:01Z' }),
    ]);
    expect(store.rows()[0].direction).toBe('up');

    store.applyAll([
      aQuote({ ticker: 'PETR4', last: '38.49', occurredAt: '2026-11-24T13:00:02Z' }),
    ]);
    expect(store.rows()[0].direction).toBe('down');
  });

  it('should ignore a quote older than the current one', () => {
    store.load([aQuote({ ticker: 'PETR4', last: '38.50', occurredAt: '2026-11-24T13:00:05Z' })]);

    store.applyAll([
      aQuote({ ticker: 'PETR4', last: '10.00', occurredAt: '2026-11-24T13:00:01Z' }),
    ]);

    expect(store.rows()[0].quote.last).toBe('38.50');
  });

  it('should fill in the first trade of an asset that had no quote', () => {
    store.load([aQuote({ ticker: 'HGLG11', last: null, occurredAt: null })]);

    store.applyAll([aQuote({ ticker: 'HGLG11', last: '158.30' })]);

    expect(store.rows()[0].quote.last).toBe('158.30');
    expect(store.rows()[0].direction).toBe('flat');
  });

  it('should restart the list when a new category is loaded', () => {
    store.load([aQuote({ ticker: 'PETR4' })]);

    store.load([aQuote({ ticker: 'MXRF11' }), aQuote({ ticker: 'HGLG11' })]);

    expect(
      store
        .rows()
        .map((row) => row.quote.ticker)
        .sort(),
    ).toEqual(['HGLG11', 'MXRF11']);
  });
});

describe('compareDecimal', () => {
  it('should compare prices with different scales without float errors', () => {
    expect(compareDecimal('0.3', '0.30')).toBe(0);
    expect(compareDecimal('100.1', '99.99')).toBe(1);
    expect(compareDecimal('9.75', '9.8')).toBe(-1);
  });

  it('should compare negative changes', () => {
    expect(compareDecimal('-2.10', '0.50')).toBe(-1);
    expect(compareDecimal('-0.5', '-2.1')).toBe(1);
  });
});
