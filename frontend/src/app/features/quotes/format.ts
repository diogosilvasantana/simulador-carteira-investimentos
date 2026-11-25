import { compareDecimal } from './quote';

/** "1128.4" vira "1.128,40". Sem passar por número, para não arredondar. */
export function formatPrice(value: string | null): string {
  if (value === null) {
    return '—';
  }
  const negative = value.startsWith('-');
  const [integer, fraction = ''] = (negative ? value.slice(1) : value).split('.');
  const grouped = integer.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  return `${negative ? '-' : ''}${grouped},${fraction.padEnd(2, '0')}`;
}

export function formatChange(value: string | null): string {
  if (value === null) {
    return '—';
  }
  const sign = compareDecimal(value, '0') > 0 ? '+' : '';
  return `${sign}${formatPrice(value)}%`;
}

const COMPACT = new Intl.NumberFormat('pt-BR', { notation: 'compact', maximumFractionDigits: 1 });

/** Quantidade negociada, abreviada: 1.250.000 vira "1,3 mi". */
export function formatVolume(value: number | null): string {
  return value === null ? '—' : COMPACT.format(value);
}
