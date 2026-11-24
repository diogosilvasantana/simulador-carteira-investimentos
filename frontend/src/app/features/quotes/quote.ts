/** Cotação como o backend publica em /api/quotes e /topic/quotes (preços em string decimal). */
export interface Quote {
  readonly ticker: string;
  readonly last: string;
  readonly bid: string | null;
  readonly ask: string | null;
  readonly previousClose: string | null;
  /** Em pontos percentuais sobre o fechamento anterior: "1.25" é alta de 1,25%. */
  readonly changePercent: string | null;
  readonly volume: number;
  readonly occurredAt: string;
  readonly source: 'LIVE' | 'REPLAY' | 'SYNTHETIC';
}

export type Direction = 'up' | 'down' | 'flat';

export interface QuoteRow {
  readonly quote: Quote;
  readonly direction: Direction;
  /** Muda a cada tick para reiniciar a animação de destaque, mesmo com a mesma direção. */
  readonly revision: number;
}

/** Compara preços decimais sem passar por float, para não errar centavos. */
export function compareDecimal(a: string, b: string): number {
  const [ai, af = ''] = a.split('.');
  const [bi, bf = ''] = b.split('.');
  const scale = Math.max(af.length, bf.length);
  const left = BigInt(ai + af.padEnd(scale, '0'));
  const right = BigInt(bi + bf.padEnd(scale, '0'));
  return left === right ? 0 : left > right ? 1 : -1;
}
