import {
  FixedIncomeProduct,
  ProductResult,
  Simulation,
  parseAmount,
  rankByNet,
  subtract,
} from './fixed-income';

const product = (id: string, gracePeriodDays = 0): FixedIncomeProduct => ({
  id,
  name: id,
  kind: 'CDB',
  indexPercentage: '100',
  incomeTaxExempt: false,
  gracePeriodDays,
});

const result = (id: string, net: string, blocked = false): ProductResult => ({
  product: product(id),
  simulation: { net } as Simulation,
  blockedByGracePeriod: blocked,
});

describe('rankByNet', () => {
  it('should put the highest net first and compare amounts as decimals', () => {
    const ranked = rankByNet([
      result('a', '9999.99'),
      result('b', '10100.10'),
      result('c', '10100.9'),
    ]);

    expect(ranked.map((r) => r.product.id)).toEqual(['c', 'b', 'a']);
  });

  it('should move products still in their grace period to the end', () => {
    const ranked = rankByNet([result('lci', '10500.00', true), result('cdb', '10100.00')]);

    expect(ranked.map((r) => r.product.id)).toEqual(['cdb', 'lci']);
  });
});

describe('parseAmount', () => {
  it('should accept the Brazilian format with thousands separators', () => {
    expect(parseAmount('10.000,50')).toBe('10000.50');
  });

  it('should accept a plain decimal', () => {
    expect(parseAmount('2500.5')).toBe('2500.5');
  });

  it('should reject zero, negatives and text', () => {
    expect(parseAmount('0')).toBeNull();
    expect(parseAmount('-10')).toBeNull();
    expect(parseAmount('dez mil')).toBeNull();
  });
});

describe('subtract', () => {
  it('should subtract money without floating point errors', () => {
    expect(subtract('10116.42', '10000.00')).toBe('116.42');
    expect(subtract('0.3', '0.1')).toBe('0.20');
    expect(subtract('10.00', '10.05')).toBe('-0.05');
  });
});
