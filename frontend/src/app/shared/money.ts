import { formatPrice } from '../features/quotes/format';
import { compareDecimal } from '../features/quotes/quote';

/** "1234.5" → "R$ 1.234,50"; negativos com o sinal antes do símbolo. */
export function brl(value: string | null | undefined): string {
  if (value === null || value === undefined) {
    return '—';
  }
  const negative = value.startsWith('-');
  const formatted = formatPrice(negative ? value.slice(1) : value);
  return `${negative ? '−' : ''}R$ ${formatted}`;
}

/** Resultado com sinal explícito: "+R$ 12,30" ou "−R$ 4,00". */
export function signedBrl(value: string | null | undefined): string {
  if (value === null || value === undefined) {
    return '—';
  }
  return tone(value) === 'up' ? `+${brl(value)}` : brl(value);
}

export function percent(value: string | null | undefined, signed = false): string {
  if (value === null || value === undefined) {
    return '—';
  }
  const negative = value.startsWith('-');
  const body = `${formatPrice(negative ? value.slice(1) : value)}%`;
  if (negative) {
    return `−${body}`;
  }
  return signed && tone(value) === 'up' ? `+${body}` : body;
}

export function tone(value: string | null | undefined): 'up' | 'down' | 'flat' {
  if (value === null || value === undefined) {
    return 'flat';
  }
  const diff = compareDecimal(value, '0');
  return diff > 0 ? 'up' : diff < 0 ? 'down' : 'flat';
}

/** Aceita "10.000,50" ou "10000.50" e devolve "10000.50"; null se não for positivo. */
export function parseMoney(text: string): string | null {
  const trimmed = text.trim().replace(/^R\$\s*/, '');
  const normalized = trimmed.includes(',') ? trimmed.replace(/\./g, '').replace(',', '.') : trimmed;
  if (!/^\d+(\.\d{1,2})?$/.test(normalized) || compareDecimal(normalized, '0') <= 0) {
    return null;
  }
  return normalized;
}
