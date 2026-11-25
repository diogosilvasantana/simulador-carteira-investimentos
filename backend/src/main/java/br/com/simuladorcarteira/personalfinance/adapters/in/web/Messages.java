package br.com.simuladorcarteira.personalfinance.adapters.in.web;

import br.com.simuladorcarteira.personalfinance.domain.BudgetItem;
import br.com.simuladorcarteira.personalfinance.domain.BudgetKind;
import br.com.simuladorcarteira.personalfinance.domain.Debt;
import br.com.simuladorcarteira.personalfinance.domain.FinancialSnapshot;
import br.com.simuladorcarteira.personalfinance.domain.FinancialStage;
import br.com.simuladorcarteira.shared.Money;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.Nullable;

/** Corpos da API; valores saem como texto decimal, como no resto do sistema. */
final class Messages {

  private Messages() {}

  record ItemRequest(
      @NotBlank @Size(max = 120) @Nullable String name,
      @NotNull @Nullable BudgetKind kind,
      @NotNull @PositiveOrZero @Digits(integer = 13, fraction = 2) @Nullable BigDecimal amount) {}

  record DebtRequest(
      @NotBlank @Size(max = 120) @Nullable String creditor,
      @NotNull @PositiveOrZero @Digits(integer = 13, fraction = 2) @Nullable BigDecimal balance,
      @PositiveOrZero @Digits(integer = 5, fraction = 4) @Nullable BigDecimal monthlyRatePercent,
      @PositiveOrZero @Digits(integer = 13, fraction = 2) @Nullable BigDecimal installment,
      boolean overdue,
      boolean collateral) {}

  record ItemMessage(long id, String name, BudgetKind kind, String amount) {
    static ItemMessage from(BudgetItem item) {
      return new ItemMessage(item.id(), item.name(), item.kind(), text(item.amount()));
    }
  }

  record DebtMessage(
      long id,
      String creditor,
      String balance,
      @Nullable String monthlyRatePercent,
      @Nullable String installment,
      boolean overdue,
      boolean collateral) {
    static DebtMessage from(Debt debt) {
      BigDecimal rate = debt.monthlyRatePercent();
      Money installment = debt.installment();
      return new DebtMessage(
          debt.id(),
          debt.creditor(),
          text(debt.balance()),
          rate == null ? null : rate.toPlainString(),
          installment == null ? null : text(installment),
          debt.overdue(),
          debt.collateral());
    }
  }

  /** {@code debts} já vem na ordem de prioridade. */
  record SnapshotMessage(
      String income,
      String essentials,
      String others,
      String installments,
      String totalDebt,
      String available,
      FinancialStage stage,
      int incompleteDebts,
      List<ItemMessage> items,
      List<DebtMessage> debts) {
    static SnapshotMessage from(FinancialSnapshot snapshot, List<BudgetItem> items) {
      return new SnapshotMessage(
          text(snapshot.income()),
          text(snapshot.essentials()),
          text(snapshot.others()),
          text(snapshot.installments()),
          text(snapshot.totalDebt()),
          text(snapshot.available()),
          snapshot.stage(),
          snapshot.incompleteDebts(),
          items.stream().map(ItemMessage::from).toList(),
          snapshot.priorities().stream().map(DebtMessage::from).toList());
    }
  }

  private static String text(Money money) {
    return money.amount().toPlainString();
  }
}
