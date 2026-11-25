package br.com.simuladorcarteira.personalfinance.domain;

import br.com.simuladorcarteira.shared.Money;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Retrato do mês: o que entra, o que sai e quais dívidas vêm primeiro. */
public record FinancialSnapshot(
    Money income,
    Money essentials,
    Money others,
    Money installments,
    Money totalDebt,
    Money available,
    FinancialStage stage,
    List<Debt> priorities,
    int incompleteDebts) {

  // Bem em risco, depois atraso, depois o juro mais caro; sem taxa conhecida vai para o fim.
  private static final Comparator<Debt> PRIORITY =
      Comparator.comparing((Debt debt) -> debt.overdue() && debt.collateral())
          .thenComparing(Debt::overdue)
          .thenComparing(
              debt -> Objects.requireNonNullElse(debt.monthlyRatePercent(), BigDecimal.ZERO))
          .reversed()
          .thenComparing(Debt::id);

  public static FinancialSnapshot of(List<BudgetItem> items, List<Debt> debts) {
    Money income = sum(items, BudgetKind.INCOME);
    Money essentials = sum(items, BudgetKind.ESSENTIAL);
    Money others = sum(items, BudgetKind.OTHER);
    Money installments =
        debts.stream()
            .map(Debt::installment)
            .filter(Objects::nonNull)
            .reduce(Money.zero(), Money::plus);
    Money total = debts.stream().map(Debt::balance).reduce(Money.zero(), Money::plus);
    Money available = income.minus(essentials).minus(others).minus(installments);
    return new FinancialSnapshot(
        income,
        essentials,
        others,
        installments,
        total,
        available,
        stage(income, available, debts),
        debts.stream().sorted(PRIORITY).toList(),
        (int) debts.stream().filter(Debt::isIncomplete).count());
  }

  private static FinancialStage stage(Money income, Money available, List<Debt> debts) {
    if (!income.isGreaterThan(Money.zero())) {
      return FinancialStage.NEEDS_DATA;
    }
    if (available.isNegative() || debts.stream().anyMatch(Debt::overdue)) {
      return FinancialStage.REORGANIZE;
    }
    return debts.isEmpty() ? FinancialStage.BUILD_RESERVE : FinancialStage.PAY_DEBTS;
  }

  private static Money sum(List<BudgetItem> items, BudgetKind kind) {
    return items.stream()
        .filter(item -> item.kind() == kind)
        .map(BudgetItem::amount)
        .reduce(Money.zero(), Money::plus);
  }
}
