import { compareDecimal } from '../quotes/quote';
import { parseMoney } from '../../shared/money';

export type BudgetKind = 'INCOME' | 'ESSENTIAL' | 'OTHER';
export type FinancialStage = 'NEEDS_DATA' | 'REORGANIZE' | 'PAY_DEBTS' | 'BUILD_RESERVE';

export interface BudgetItem {
  readonly id: number;
  readonly name: string;
  readonly kind: BudgetKind;
  readonly amount: string;
}

export interface Debt {
  readonly id: number;
  readonly creditor: string;
  readonly balance: string;
  readonly monthlyRatePercent: string | null;
  readonly installment: string | null;
  readonly overdue: boolean;
  readonly collateral: boolean;
}

/** Formato de /api/personal-finance; `debts` já vem na ordem de prioridade. */
export interface Snapshot {
  readonly income: string;
  readonly essentials: string;
  readonly others: string;
  readonly installments: string;
  readonly totalDebt: string;
  readonly available: string;
  readonly stage: FinancialStage;
  readonly incompleteDebts: number;
  readonly items: readonly BudgetItem[];
  readonly debts: readonly Debt[];
}

export const KIND_LABELS: Record<BudgetKind, string> = {
  INCOME: 'Renda',
  ESSENTIAL: 'Essencial',
  OTHER: 'Outro gasto',
};

export const STAGES: Record<FinancialStage, { readonly title: string; readonly text: string }> = {
  NEEDS_DATA: {
    title: 'Conte como está o mês',
    text: 'Cadastre a renda, os gastos da casa e as dívidas para ver o que vem primeiro.',
  },
  REORGANIZE: {
    title: 'Proteger a casa e reorganizar',
    text: 'O mês não fecha ou há dívida vencida. O básico da casa vem antes de qualquer acordo.',
  },
  PAY_DEBTS: {
    title: 'Sair das dívidas',
    text: 'O mês fecha e nada está vencido. O que sobra ataca a dívida mais cara.',
  },
  BUILD_RESERVE: {
    title: 'Formar a reserva',
    text: 'Sem dívidas: o próximo passo é guardar, mesmo que pouco, com liquidez.',
  },
};

/** Campo opcional: vazio vira null (desconhecido); texto inválido vira undefined. */
export function optionalMoney(text: string): string | null | undefined {
  return text.trim() === '' ? null : (parseMoney(text) ?? undefined);
}

/** Aceita "2,88", "2.88%" e até 4 casas; vazio é desconhecido, zero é permitido. */
export function optionalRate(text: string): string | null | undefined {
  const value = text.trim().replace(/%$/, '').trim().replace(',', '.');
  if (value === '') {
    return null;
  }
  return /^\d{1,5}(\.\d{1,4})?$/.test(value) ? value : undefined;
}

export function isNegative(value: string): boolean {
  return compareDecimal(value, '0') < 0;
}
