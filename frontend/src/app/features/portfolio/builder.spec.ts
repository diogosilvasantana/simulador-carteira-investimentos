import { PROFILES, fromCents, planClass, toCents } from './builder';

describe('builder', () => {
  it('should keep every profile at 100%', () => {
    for (const profile of PROFILES) {
      const sum = Object.values(profile.weights).reduce((a, b) => a + b, 0);
      expect(sum).toBe(100);
    }
  });

  it('should convert money to cents without floating point', () => {
    expect(toCents('38.7')).toBe(3870);
    expect(toCents('0.05')).toBe(5);
    expect(fromCents(123456)).toBe('1234.56');
  });

  it('should split the class budget equally and buy whole units', () => {
    const plan = planClass(100_000, [
      { ticker: 'PETR4', name: 'Petrobras', last: '38.00' },
      { ticker: 'VALE3', name: 'Vale', last: '61.00' },
    ]);

    // R$ 1.000 → R$ 499,75 para cada um (com a folga dos custos)
    expect(plan.map((o) => o.quantity)).toEqual([13, 8]);
    expect(plan[0].totalCents).toBe(49_400);
  });

  it('should drop assets whose price does not fit the share', () => {
    const plan = planClass(5_000, [{ ticker: 'BOVA11', name: 'ETF', last: '131.40' }]);

    expect(plan).toEqual([]);
  });
});
