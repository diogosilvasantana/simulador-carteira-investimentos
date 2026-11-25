import { formatChange, formatPrice, formatVolume } from './format';

describe('format', () => {
  it('should format prices in Brazilian notation without rounding', () => {
    expect(formatPrice('1128.4')).toBe('1.128,40');
    expect(formatPrice('9.75')).toBe('9,75');
    expect(formatPrice(null)).toBe('—');
  });

  it('should sign the change and add the percent symbol', () => {
    expect(formatChange('1.25')).toBe('+1,25%');
    expect(formatChange('-0.51')).toBe('-0,51%');
    expect(formatChange('0.00')).toBe('0,00%');
  });

  it('should abbreviate large volumes', () => {
    expect(formatVolume(1_250_000)).toMatch(/^1,3\s?mi/);
    expect(formatVolume(null)).toBe('—');
  });
});
