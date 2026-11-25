package br.com.simuladorcarteira.personalfinance.domain;

import br.com.simuladorcarteira.shared.Money;
import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

/** Uma dívida. Taxa e parcela ficam nulas enquanto não forem conhecidas; nunca valem zero. */
public record Debt(
    long id,
    String creditor,
    Money balance,
    @Nullable BigDecimal monthlyRatePercent,
    @Nullable Money installment,
    boolean overdue,
    boolean collateral) {

  public Debt {
    if (creditor.isBlank()) {
      throw new IllegalArgumentException("Informe o credor");
    }
    if (balance.isNegative()
        || (installment != null && installment.isNegative())
        || (monthlyRatePercent != null && monthlyRatePercent.signum() < 0)) {
      throw new IllegalArgumentException("Saldo, taxa e parcela não podem ser negativos");
    }
  }

  public boolean isIncomplete() {
    return monthlyRatePercent == null || installment == null;
  }
}
