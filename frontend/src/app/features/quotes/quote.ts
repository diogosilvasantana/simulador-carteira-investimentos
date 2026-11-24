/**
 * Ativo com a última cotação, como o backend publica em /api/assets e nos tópicos STOMP. Preços
 * vêm em texto decimal; os campos da cotação ficam nulos quando o ativo ainda não negociou e os do
 * ativo, quando o catálogo ainda não o conhece.
 */
export interface Quote {
  readonly ticker: string;
  readonly name: string | null;
  readonly type: AssetType | null;
  readonly category: string | null;
  readonly listingSegment: ListingSegment | null;
  readonly last: string | null;
  readonly bid: string | null;
  readonly ask: string | null;
  readonly previousClose: string | null;
  /** Em pontos percentuais sobre o fechamento anterior: "1.25" é alta de 1,25%. */
  readonly changePercent: string | null;
  readonly volume: number | null;
  readonly occurredAt: string | null;
  readonly source: 'LIVE' | 'REPLAY' | 'SYNTHETIC' | null;
}

export type AssetType =
  'STOCK' | 'UNIT' | 'REAL_ESTATE_FUND' | 'AGRIBUSINESS_FUND' | 'ETF' | 'BDR' | 'BDR_ETF' | 'OTHER';

export type ListingSegment =
  'NOVO_MERCADO' | 'LEVEL_2' | 'LEVEL_1' | 'BOVESPA_MAIS' | 'BOVESPA_MAIS_LEVEL_2';

export type Direction = 'up' | 'down' | 'flat';

export interface QuoteRow {
  readonly quote: Quote;
  readonly direction: Direction;
  /** Muda a cada atualização para reiniciar a animação de destaque, mesmo com a mesma direção. */
  readonly revision: number;
}

/** Compara preços decimais sem passar por float, para não errar centavos. */
export function compareDecimal(a: string, b: string): number {
  const [left, right] = [a, b].map(toScaled);
  const scale = Math.max(left.scale, right.scale);
  const l = left.digits * 10n ** BigInt(scale - left.scale);
  const r = right.digits * 10n ** BigInt(scale - right.scale);
  return l === r ? 0 : l > r ? 1 : -1;
}

function toScaled(value: string): { digits: bigint; scale: number } {
  const negative = value.startsWith('-');
  const [integer, fraction = ''] = (negative ? value.slice(1) : value).split('.');
  const digits = BigInt(integer + fraction);
  return { digits: negative ? -digits : digits, scale: fraction.length };
}
