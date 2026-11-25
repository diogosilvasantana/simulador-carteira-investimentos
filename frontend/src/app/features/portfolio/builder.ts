import type { AssetClassSlug } from '../../core/portfolio';

/** Perfis prontos para quem não sabe por onde começar; o usuário ajusta depois. */
export interface Profile {
  readonly id: 'conservadora' | 'moderada' | 'arrojada';
  readonly label: string;
  readonly description: string;
  readonly weights: Readonly<Record<AssetClassSlug, number>>;
}

export const PROFILES: readonly Profile[] = [
  {
    id: 'conservadora',
    label: 'Conservadora',
    description: 'Quase tudo em renda fixa, um pouco de bolsa para aprender.',
    weights: { acoes: 15, 'renda-fixa': 70, fiagros: 0, fiis: 15, bdrs: 0, etfs: 0 },
  },
  {
    id: 'moderada',
    label: 'Moderada',
    description: 'Metade protegida, metade buscando crescer.',
    weights: { acoes: 30, 'renda-fixa': 40, fiagros: 5, fiis: 15, bdrs: 0, etfs: 10 },
  },
  {
    id: 'arrojada',
    label: 'Arrojada',
    description: 'Mais bolsa, aceitando oscilar para buscar mais retorno.',
    weights: { acoes: 45, 'renda-fixa': 15, fiagros: 5, fiis: 15, bdrs: 10, etfs: 10 },
  },
];

/** "38.7" → 3870 centavos, sem passar por ponto flutuante. */
export function toCents(value: string): number {
  const [integer, fraction = ''] = value.split('.');
  return Number(integer) * 100 + Number(fraction.padEnd(2, '0').slice(0, 2));
}

export function fromCents(cents: number): string {
  const sign = cents < 0 ? '-' : '';
  const abs = Math.abs(cents);
  return `${sign}${Math.floor(abs / 100)}.${String(abs % 100).padStart(2, '0')}`;
}

export interface PlannedOrder {
  readonly ticker: string;
  readonly name: string;
  readonly priceCents: number;
  readonly quantity: number;
  readonly totalCents: number;
}

// Folga para os custos da B3 (0,03%), para a última compra não esbarrar no caixa.
const COST_MARGIN = 0.9995;

/**
 * Divide o valor da classe igualmente entre os ativos escolhidos e compra a maior quantidade
 * inteira que cabe em cada parte; o que sobra fica no caixa.
 */
export function planClass(
  budgetCents: number,
  assets: readonly { ticker: string; name: string; last: string }[],
): PlannedOrder[] {
  if (assets.length === 0 || budgetCents <= 0) {
    return [];
  }
  const share = Math.floor((budgetCents * COST_MARGIN) / assets.length);
  return assets
    .map((asset) => {
      const priceCents = toCents(asset.last);
      const quantity = priceCents > 0 ? Math.floor(share / priceCents) : 0;
      return {
        ticker: asset.ticker,
        name: asset.name,
        priceCents,
        quantity,
        totalCents: quantity * priceCents,
      };
    })
    .filter((order) => order.quantity > 0);
}
