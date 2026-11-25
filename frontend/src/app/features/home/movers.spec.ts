import { describe, expect, it } from 'vitest';
import { Quote } from '../quotes/quote';
import { movers } from './movers';

function quote(ticker: string, changePercent: string | null, volume = 0): Quote {
  return {
    ticker,
    name: ticker,
    type: 'STOCK',
    category: 'acoes',
    listingSegment: null,
    last: changePercent === null ? null : '10.00',
    bid: null,
    ask: null,
    previousClose: '10.00',
    changePercent,
    volume,
    occurredAt: null,
    source: 'LIVE',
  };
}

describe('movers', () => {
  it('should rank gainers from the biggest rise', () => {
    const result = movers([quote('AAAA3', '1.5'), quote('BBBB3', '10.25'), quote('CCCC3', '-2')]);

    expect(result.gainers.map((q) => q.ticker)).toEqual(['BBBB3', 'AAAA3']);
  });

  it('should rank losers from the biggest drop', () => {
    const result = movers([quote('AAAA3', '-1.5'), quote('BBBB3', '-10.25'), quote('CCCC3', '2')]);

    expect(result.losers.map((q) => q.ticker)).toEqual(['BBBB3', 'AAAA3']);
  });

  it('should ignore assets that have not traded yet', () => {
    const result = movers([quote('AAAA3', null, 1_000_000), quote('BBBB3', '0', 10)]);

    expect(result.active.map((q) => q.ticker)).toEqual(['BBBB3']);
  });
});
