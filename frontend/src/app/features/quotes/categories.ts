import { AssetType, ListingSegment } from './quote';

export interface Category {
  readonly slug: string;
  readonly label: string;
}

/** Mesma ordem e identificadores de AssetCategory no backend (ADR-010). */
export const CATEGORIES: readonly Category[] = [
  { slug: 'acoes', label: 'Ações' },
  { slug: 'fiis', label: 'FIIs' },
  { slug: 'fiagros', label: 'Fiagros' },
  { slug: 'etfs', label: 'ETFs' },
  { slug: 'bdrs', label: 'BDRs' },
];

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
