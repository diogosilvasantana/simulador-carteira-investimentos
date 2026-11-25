import { isNegative, optionalMoney, optionalRate } from './today';

describe('today', () => {
  it('should treat an empty optional value as unknown, not zero', () => {
    expect(optionalMoney(' ')).toBeNull();
    expect(optionalRate('')).toBeNull();
  });

  it('should read brazilian money and rates', () => {
    expect(optionalMoney('1.379,51')).toBe('1379.51');
    expect(optionalRate('2,88%')).toBe('2.88');
    expect(optionalRate('0')).toBe('0');
  });

  it('should flag invalid text', () => {
    expect(optionalMoney('abc')).toBeUndefined();
    expect(optionalRate('-1')).toBeUndefined();
  });

  it('should tell when the month does not close', () => {
    expect(isNegative('-0.01')).toBe(true);
    expect(isNegative('0.00')).toBe(false);
  });
});
