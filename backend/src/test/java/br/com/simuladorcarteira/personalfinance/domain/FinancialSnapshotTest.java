package br.com.simuladorcarteira.personalfinance.domain;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.simuladorcarteira.shared.Money;
import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

class FinancialSnapshotTest {

  private static final List<BudgetItem> HOME =
      List.of(
          item("Salário", BudgetKind.INCOME, "5000.00"),
          item("Mercado", BudgetKind.ESSENTIAL, "2000.10"),
          item("Passeio", BudgetKind.OTHER, "99.90"));

  @Test
  void shouldSubtractExpensesAndKnownInstallmentsFromIncome() {
    FinancialSnapshot snapshot =
        FinancialSnapshot.of(HOME, List.of(debt(1, "1", "1000.00", false, false)));

    assertThat(snapshot.available()).isEqualTo(Money.brl("1900.00"));
  }

  @Test
  void shouldReorganizeWhenTheMonthDoesNotClose() {
    FinancialSnapshot snapshot =
        FinancialSnapshot.of(HOME, List.of(debt(1, "1", "3000.00", false, false)));

    assertThat(snapshot.stage()).isEqualTo(FinancialStage.REORGANIZE);
  }

  @Test
  void shouldReorganizeWhenAnyDebtIsOverdueEvenWithMoneyLeft() {
    FinancialSnapshot snapshot =
        FinancialSnapshot.of(HOME, List.of(debt(1, "1", "100.00", true, false)));

    assertThat(snapshot.stage()).isEqualTo(FinancialStage.REORGANIZE);
  }

  @Test
  void shouldPayDebtsWhenTheyAreOnTimeAndTheMonthCloses() {
    FinancialSnapshot snapshot =
        FinancialSnapshot.of(HOME, List.of(debt(1, "1", "100.00", false, false)));

    assertThat(snapshot.stage()).isEqualTo(FinancialStage.PAY_DEBTS);
  }

  @Test
  void shouldBuildAReserveWhenThereIsNoDebt() {
    assertThat(FinancialSnapshot.of(HOME, List.of()).stage())
        .isEqualTo(FinancialStage.BUILD_RESERVE);
  }

  @Test
  void shouldAskForDataBeforeAnyIncomeIsInformed() {
    assertThat(FinancialSnapshot.of(List.of(), List.of()).stage())
        .isEqualTo(FinancialStage.NEEDS_DATA);
  }

  @Test
  void shouldRankOverdueCollateralThenOverdueThenHighestKnownRate() {
    FinancialSnapshot snapshot =
        FinancialSnapshot.of(
            HOME,
            List.of(
                debt(1, null, "1.00", false, false),
                debt(2, "2", "1.00", false, false),
                debt(3, "9", "1.00", false, false),
                debt(4, "5", "1.00", true, false),
                debt(5, "1", "1.00", true, true)));

    assertThat(snapshot.priorities()).extracting(Debt::id).containsExactly(5L, 4L, 3L, 2L, 1L);
  }

  @Test
  void shouldCountDebtsWithMissingRateOrInstallment() {
    FinancialSnapshot snapshot =
        FinancialSnapshot.of(
            HOME,
            List.of(
                debt(1, null, "1.00", false, false),
                debt(2, "1", null, false, false),
                debt(3, "1", "1.00", false, false)));

    assertThat(snapshot.incompleteDebts()).isEqualTo(2);
  }

  private static BudgetItem item(String name, BudgetKind kind, String amount) {
    return new BudgetItem(0, name, kind, Money.brl(amount));
  }

  private static Debt debt(
      long id,
      @Nullable String rate,
      @Nullable String installment,
      boolean overdue,
      boolean collateral) {
    return new Debt(
        id,
        "Credor " + id,
        Money.brl("10000.00"),
        rate == null ? null : new BigDecimal(rate),
        installment == null ? null : Money.brl(installment),
        overdue,
        collateral);
  }
}
