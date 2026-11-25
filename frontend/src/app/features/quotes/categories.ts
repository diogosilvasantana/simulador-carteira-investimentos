import { AssetType, ListingSegment } from './quote';

export interface Category {
  readonly slug: string;
  readonly label: string;
  /** Uma frase para quem está começando entender a classe. */
  readonly description: string;
  /** Token de cor da classe no Design System. */
  readonly color: string;
}

/** Mesmos identificadores de AssetCategory no backend (ADR-010). */
export const CATEGORIES: readonly Category[] = [
  {
    slug: 'acoes',
    label: 'Ações',
    description: 'Sócio de empresas, com dividendos e valorização.',
    color: 'var(--asset-acoes)',
  },
  {
    slug: 'fiis',
    label: 'FIIs',
    description: 'Renda mensal de imóveis e recebíveis.',
    color: 'var(--asset-fiis)',
  },
  {
    slug: 'fiagros',
    label: 'Fiagros',
    description: 'Fundos do agronegócio negociados na bolsa.',
    color: 'var(--asset-fiagros)',
  },
  {
    slug: 'etfs',
    label: 'ETFs',
    description: 'Um índice inteiro numa cota só.',
    color: 'var(--asset-etfs)',
  },
  {
    slug: 'bdrs',
    label: 'BDRs',
    description: 'Empresas de fora, negociadas em reais.',
    color: 'var(--asset-bdrs)',
  },
];

/** Cor da classe pelo tipo do ativo, para o monograma do ticker. */
export function typeColor(type: AssetType | null): string {
  switch (type) {
    case 'REAL_ESTATE_FUND':
      return 'var(--asset-fiis)';
    case 'AGRIBUSINESS_FUND':
      return 'var(--asset-fiagros)';
    case 'ETF':
      return 'var(--asset-etfs)';
    case 'BDR':
    case 'BDR_ETF':
      return 'var(--asset-bdrs)';
    default:
      return 'var(--asset-acoes)';
  }
}

export const DEFAULT_CATEGORY = CATEGORIES[0].slug;

export const TYPE_LABELS: Readonly<Record<AssetType, string>> = {
  STOCK: 'Ação',
  UNIT: 'Unit',
  REAL_ESTATE_FUND: 'FII',
  AGRIBUSINESS_FUND: 'Fiagro',
  ETF: 'ETF',
  BDR: 'BDR',
  BDR_ETF: 'BDR de ETF',
  OTHER: 'Outro',
};

export const LISTING_LABELS: Readonly<Record<ListingSegment, string>> = {
  NOVO_MERCADO: 'Novo Mercado',
  LEVEL_2: 'Nível 2',
  LEVEL_1: 'Nível 1',
  BOVESPA_MAIS: 'Bovespa Mais',
  BOVESPA_MAIS_LEVEL_2: 'Bovespa Mais N2',
};
