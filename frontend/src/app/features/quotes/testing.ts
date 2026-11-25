import { Quote, QuoteRow } from './quote';

/** Cotação de teste: só os campos que importam em cada caso precisam ser informados. */
export function aQuote(overrides: Partial<Quote> & Pick<Quote, 'ticker'>): Quote {
  return {
    name: null,
    type: 'STOCK',
    category: 'acoes',
    listingSegment: null,
    last: '10.00',
    bid: null,
    ask: null,
    previousClose: null,
    changePercent: null,
    volume: 100,
    occurredAt: '2026-11-24T13:00:00Z',
    source: 'LIVE',
    ...overrides,
  };
}

export function aRow(overrides: Partial<Quote> & Pick<Quote, 'ticker'>): QuoteRow {
  return { quote: aQuote(overrides), direction: 'flat', revision: 0 };
}
