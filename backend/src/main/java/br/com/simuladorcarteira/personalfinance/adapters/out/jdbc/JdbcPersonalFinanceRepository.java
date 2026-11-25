package br.com.simuladorcarteira.personalfinance.adapters.out.jdbc;

import br.com.simuladorcarteira.personalfinance.application.PersonalFinanceRepository;
import br.com.simuladorcarteira.personalfinance.domain.BudgetItem;
import br.com.simuladorcarteira.personalfinance.domain.BudgetKind;
import br.com.simuladorcarteira.personalfinance.domain.Debt;
import br.com.simuladorcarteira.shared.Money;
import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty("simulador.personal-finance.enabled")
class JdbcPersonalFinanceRepository implements PersonalFinanceRepository {

  private final JdbcClient jdbc;

  JdbcPersonalFinanceRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public List<BudgetItem> items() {
    return jdbc.sql("select id, name, kind, amount from budget_item order by id")
        .query(
            (row, n) ->
                new BudgetItem(
                    row.getLong("id"),
                    row.getString("name"),
                    BudgetKind.valueOf(row.getString("kind")),
                    Money.brl(row.getBigDecimal("amount"))))
        .list();
  }

  @Override
  public List<Debt> debts() {
    return jdbc.sql(
            """
            select id, creditor, balance, monthly_rate_percent, installment, overdue, collateral
            from debt order by id
            """)
        .query(
            (row, n) ->
                new Debt(
                    row.getLong("id"),
                    row.getString("creditor"),
                    Money.brl(row.getBigDecimal("balance")),
                    row.getBigDecimal("monthly_rate_percent"),
                    money(row.getBigDecimal("installment")),
                    row.getBoolean("overdue"),
                    row.getBoolean("collateral")))
        .list();
  }

  @Override
  public void add(BudgetItem item) {
    jdbc.sql("insert into budget_item (name, kind, amount) values (:name, :kind, :amount)")
        .param("name", item.name())
        .param("kind", item.kind().name())
        .param("amount", item.amount().amount())
        .update();
  }

  @Override
  public void add(Debt debt) {
    Money installment = debt.installment();
    jdbc.sql(
            """
            insert into debt
              (creditor, balance, monthly_rate_percent, installment, overdue, collateral)
            values (:creditor, :balance, :rate, :installment, :overdue, :collateral)
            """)
        .param("creditor", debt.creditor())
        .param("balance", debt.balance().amount())
        .param("rate", debt.monthlyRatePercent(), java.sql.Types.NUMERIC)
        .param(
            "installment",
            installment == null ? null : installment.amount(),
            java.sql.Types.NUMERIC)
        .param("overdue", debt.overdue())
        .param("collateral", debt.collateral())
        .update();
  }

  @Override
  public boolean removeItem(long id) {
    return jdbc.sql("delete from budget_item where id = :id").param("id", id).update() > 0;
  }

  @Override
  public boolean removeDebt(long id) {
    return jdbc.sql("delete from debt where id = :id").param("id", id).update() > 0;
  }

  private static @Nullable Money money(@Nullable BigDecimal amount) {
    return amount == null ? null : Money.brl(amount);
  }
}
