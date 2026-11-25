import type { AssetClassSlug } from '../core/portfolio';

export interface AssetClassInfo {
  readonly slug: AssetClassSlug;
  readonly label: string;
  readonly color: string;
}

/** Classes na ordem fixa do design system: alocação e gráficos sempre seguem esta ordem. */
export const ASSET_CLASSES: readonly AssetClassInfo[] = [
  { slug: 'acoes', label: 'Ações', color: 'var(--asset-acoes)' },
  { slug: 'renda-fixa', label: 'Renda fixa', color: 'var(--asset-renda-fixa)' },
  { slug: 'fiagros', label: 'Fiagros', color: 'var(--asset-fiagros)' },
  { slug: 'fiis', label: 'FIIs', color: 'var(--asset-fiis)' },
  { slug: 'bdrs', label: 'BDRs', color: 'var(--asset-bdrs)' },
  { slug: 'etfs', label: 'ETFs', color: 'var(--asset-etfs)' },
];

export function classInfo(slug: string): AssetClassInfo {
  return ASSET_CLASSES.find((c) => c.slug === slug) ?? ASSET_CLASSES[0];
}
