import { compareDecimal } from '../quotes/quote';

/** Formatos de GET /api/fixed-income/*. Valores decimais chegam como texto (ver Quote). */
export interface IndexRate {
  readonly index: 'CDI' | 'SELIC_TARGET' | 'IPCA';
  readonly date: string;
  readonly percent: string;
  readonly unit: string;
  readonly annualPercent: string | null;
}

export type ProductKind = 'TESOURO_SELIC' | 'CDB' | 'LCI' | 'LCA';

export interface FixedIncomeProduct {
  readonly id: string;
  readonly name: string;
  readonly kind: ProductKind;
  readonly indexPercentage: string;
  readonly incomeTaxExempt: boolean;
  readonly gracePeriodDays: number;
}

export interface Simulation {
  readonly principal: string;
  readonly gross: string;
  readonly grossYield: string;
  readonly iof: string;
  readonly incomeTax: string;
  readonly net: string;
  readonly holdingDays: number;
  readonly iofRatePercent: string;
  readonly incomeTaxRatePercent: string;
  readonly ratesUntil: string;
  readonly projectedBusinessDays: number;
}

export interface SimulationRequest {
  readonly principal: string;
  readonly indexPercentage: string;
  readonly start: string;
  readonly end: string;
  readonly incomeTaxExempt: boolean;
}

export interface ProductResult {
  readonly product: FixedIncomeProduct;
  readonly simulation: Simulation;
  /** O prazo não cumpre a carência: na vida real esse resgate não seria aceito. */
  readonly blockedByGracePeriod: boolean;
}

export const INDEX_LABELS: Readonly<Record<IndexRate['index'], string>> = {
  CDI: 'CDI',
  SELIC_TARGET: 'Selic meta',
  IPCA: 'IPCA',
};

export const KIND_LABELS: Readonly<Record<ProductKind, string>> = {
  TESOURO_SELIC: 'Tesouro',
  CDB: 'CDB',
  LCI: 'LCI',
  LCA: 'LCA',
};

export function requestFor(
  product: FixedIncomeProduct,
  principal: string,
  start: string,
  end: string,
): SimulationRequest {
  return {
    principal,
    indexPercentage: product.indexPercentage,
    start,
    end,
    incomeTaxExempt: product.incomeTaxExempt,
  };
}

/** Maior líquido primeiro; quem esbarra na carência vai para o fim, porque não daria para resgatar. */
export function rankByNet(results: readonly ProductResult[]): ProductResult[] {
  return [...results].sort(
    (a, b) =>
      Number(a.blockedByGracePeriod) - Number(b.blockedByGracePeriod) ||
      compareDecimal(b.simulation.net, a.simulation.net),
  );
}

/** "2026-11-24" a partir de uma data local, sem passar por UTC (que viraria o dia à noite). */
export function isoDate(date: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

/** Aceita "10.000,50" ou "10000.50" e devolve "10000.50"; null se não for um valor positivo. */
export function parseAmount(text: string): string | null {
  const trimmed = text.trim();
  const normalized = trimmed.includes(',') ? trimmed.replace(/\./g, '').replace(',', '.') : trimmed;
  if (!/^\d+(\.\d{1,2})?$/.test(normalized) || compareDecimal(normalized, '0') <= 0) {
    return null;
  }
  return normalized;
}

/** a − b em texto, com 2 casas, sem passar por ponto flutuante (valores de dinheiro). */
export function subtract(a: string, b: string): string {
  const cents = (v: string) => {
    const negative = v.startsWith('-');
    const [integer, fraction = ''] = (negative ? v.slice(1) : v).split('.');
    const value = BigInt(integer + fraction.padEnd(2, '0').slice(0, 2));
    return negative ? -value : value;
  };
  const diff = cents(a) - cents(b);
  const abs = diff < 0n ? -diff : diff;
  const text = abs.toString().padStart(3, '0');
  return `${diff < 0n ? '-' : ''}${text.slice(0, -2)}.${text.slice(-2)}`;
}
