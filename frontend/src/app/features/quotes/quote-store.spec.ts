import { TestBed } from '@angular/core/testing';
import { Quote, compareDecimal } from './quote';
import { QuoteStore } from './quote-store';

function quote(ticker: string, last: string, occurredAt = '2026-11-24T13:00:00Z'): Quote {
  return {
    ticker,
    last,
    bid: null,
    ask: null,
    previousClose: null,
    changePercent: null,
    volume: 100,
    occurredAt,
    source: 'SYNTHETIC',
  };
}

describe('QuoteStore', () => {
  let store: QuoteStore;

  beforeEach(() => {
    store = TestBed.inject(QuoteStore);
  });

  it('should sort rows by ticker', () => {
    store.load([quote('VALE3', '61.20'), quote('PETR4', '38.50')]);

    expect(store.rows().map((r) => r.quote.ticker)).toEqual(['PETR4', 'VALE3']);
  });

  it('should mark a higher price as up and a lower one as down', () => {
    store.apply(quote('PETR4', '38.50', '2026-11-24T13:00:00Z'));
    store.apply(quote('PETR4', '38.51', '2026-11-24T13:00:01Z'));
    expect(store.rows()[0].direction).toBe('up');

    store.apply(quote('PETR4', '38.49', '2026-11-24T13:00:02Z'));
    expect(store.rows()[0].direction).toBe('down');
  });

  it('should apply a batch keeping the newest tick of each ticker', () => {
    store.applyAll([
      quote('PETR4', '38.50', '2026-11-24T13:00:00Z'),
      quote('VALE3', '61.20', '2026-11-24T13:00:00Z'),
      quote('PETR4', '38.60', '2026-11-24T13:00:01Z'),
    ]);

    expect(store.rows().map((r) => [r.quote.ticker, r.quote.last, r.direction])).toEqual([
      ['PETR4', '38.60', 'up'],
      ['VALE3', '61.20', 'flat'],
    ]);
  });

  it('should ignore a tick older than the current one', () => {
    store.apply(quote('PETR4', '38.50', '2026-11-24T13:00:05Z'));

    store.apply(quote('PETR4', '10.00', '2026-11-24T13:00:01Z'));

    expect(store.rows()[0].quote.last).toBe('38.50');
  });
});

describe('compareDecimal', () => {
  it('should compare prices with different scales without float errors', () => {
    expect(compareDecimal('0.3', '0.30')).toBe(0);
    expect(compareDecimal('100.1', '99.99')).toBe(1);
    expect(compareDecimal('9.75', '9.8')).toBe(-1);
  });
});
