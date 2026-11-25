package br.com.simuladorcarteira.portfolio.adapters.out.jdbc;

import br.com.simuladorcarteira.portfolio.application.PortfolioRepository;
import br.com.simuladorcarteira.portfolio.domain.AssetClass;
import br.com.simuladorcarteira.portfolio.domain.Contribution;
import br.com.simuladorcarteira.portfolio.domain.Execution;
import br.com.simuladorcarteira.portfolio.domain.FixedIncomeHolding;
import br.com.simuladorcarteira.portfolio.domain.OrderSide;
import br.com.simuladorcarteira.portfolio.domain.Portfolio;
import br.com.simuladorcarteira.portfolio.domain.PortfolioState;
import br.com.simuladorcarteira.portfolio.domain.Position;
import br.com.simuladorcarteira.shared.Money;
import br.com.simuladorcarteira.shared.Ticker;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.Function;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * A carteira é lida inteira, alterada no domínio e gravada de volta na mesma transação, com a linha
 * da carteira travada: duas ordens ao mesmo tempo esperam uma pela outra em vez de gastar o mesmo
 * caixa. Execuções, aportes e aplicações só crescem, então gravamos apenas os novos.
 */
@Repository
class JdbcPortfolioRepository implements PortfolioRepository {

  // Só existe uma carteira até o login chegar; as tabelas já separam por portfolio_id.
  private static final long PORTFOLIO_ID = 1;

  private final JdbcClient jdbc;

  JdbcPortfolioRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  @Transactional
  public <T> T update(Function<Portfolio, T> change) {
    PortfolioState before = lockAndLoad();
    Portfolio portfolio = Portfolio.restore(before);
    T result = change.apply(portfolio);
    save(before, portfolio.state());
    return result;
  }

  @Override
  @Transactional
  public void reset() {
    jdbc.sql("delete from portfolio where id = :id").param("id", PORTFOLIO_ID).update();
  }

  private PortfolioState lockAndLoad() {
    boolean exists =
        jdbc.sql("select id from portfolio where id = :id for update")
            .param("id", PORTFOLIO_ID)
            .query(Long.class)
            .optional()
            .isPresent();
    if (!exists) {
      PortfolioState empty = PortfolioState.empty();
      jdbc.sql(
              """
              insert into portfolio (id, cash, contributed, last_id)
              values (:id, :cash, :contributed, :lastId)
              """)
          .param("id", PORTFOLIO_ID)
          .param("cash", empty.cash().amount())
          .param("contributed", empty.contributed().amount())
          .param("lastId", empty.lastId())
          .update();
      return empty;
    }
    return load();
  }

  private PortfolioState load() {
    record Head(Money cash, Money contributed, long lastId) {}
    Head head =
        jdbc.sql("select cash, contributed, last_id from portfolio where id = :id")
            .param("id", PORTFOLIO_ID)
            .query(
                (rs, n) ->
                    new Head(money(rs, "cash"), money(rs, "contributed"), rs.getLong("last_id")))
            .single();
    return new PortfolioState(
        head.cash(),
        head.contributed(),
        head.lastId(),
        children("portfolio_position", "ticker", JdbcPortfolioRepository::position),
        children("portfolio_fixed_income", "id", JdbcPortfolioRepository::fixedIncome),
        children("portfolio_contribution", "seq", JdbcPortfolioRepository::contribution),
        children("portfolio_execution", "id", JdbcPortfolioRepository::execution));
  }

  private <T> List<T> children(String table, String orderBy, RowReader<T> reader) {
    // Nomes de tabela e coluna vêm só das constantes desta classe, nunca de fora.
    return jdbc.sql("select * from " + table + " where portfolio_id = :id order by " + orderBy)
        .param("id", PORTFOLIO_ID)
        .query((rs, n) -> reader.read(rs))
        .list();
  }

  private void save(PortfolioState before, PortfolioState after) {
    jdbc.sql(
            "update portfolio set cash = :cash, contributed = :contributed, last_id = :lastId"
                + " where id = :id")
        .param("cash", after.cash().amount())
        .param("contributed", after.contributed().amount())
        .param("lastId", after.lastId())
        .param("id", PORTFOLIO_ID)
        .update();

    if (!after.positions().equals(before.positions())) {
      jdbc.sql("delete from portfolio_position where portfolio_id = :id")
          .param("id", PORTFOLIO_ID)
          .update();
      after.positions().forEach(this::insert);
    }
    after.executions().stream().skip(before.executions().size()).forEach(this::insert);
    after.fixedIncome().stream().skip(before.fixedIncome().size()).forEach(this::insert);
    List<Contribution> contributions = after.contributions();
    for (int seq = before.contributions().size(); seq < contributions.size(); seq++) {
      insert(seq + 1, contributions.get(seq));
    }
  }

  private void insert(Position p) {
    jdbc.sql(
            """
            insert into portfolio_position
              (portfolio_id, ticker, name, asset_class, quantity, average_price, realized_pnl)
            values (:id, :ticker, :name, :assetClass, :quantity, :averagePrice, :realizedPnl)
            """)
        .param("id", PORTFOLIO_ID)
        .param("ticker", p.ticker().value())
        .param("name", p.name())
        .param("assetClass", p.assetClass().name())
        .param("quantity", p.quantity())
        .param("averagePrice", p.averagePrice().amount())
        .param("realizedPnl", p.realizedPnl().amount())
        .update();
  }

  private void insert(Execution e) {
    jdbc.sql(
            """
            insert into portfolio_execution
              (portfolio_id, id, side, ticker, quantity, price, fees, total, executed_at)
            values (:portfolioId, :id, :side, :ticker, :quantity, :price, :fees, :total, :at)
            """)
        .param("portfolioId", PORTFOLIO_ID)
        .param("id", e.id())
        .param("side", e.side().name())
        .param("ticker", e.ticker().value())
        .param("quantity", e.quantity())
        .param("price", e.price().amount())
        .param("fees", e.fees().amount())
        .param("total", e.total().amount())
        .param("at", timestamp(e.executedAt()))
        .update();
  }

  private void insert(FixedIncomeHolding f) {
    jdbc.sql(
            """
            insert into portfolio_fixed_income
              (portfolio_id, id, product_id, name, principal, index_percentage,
               income_tax_exempt, start_date)
            values (:portfolioId, :id, :productId, :name, :principal, :indexPercentage,
                    :exempt, :start)
            """)
        .param("portfolioId", PORTFOLIO_ID)
        .param("id", f.id())
        .param("productId", f.productId())
        .param("name", f.name())
        .param("principal", f.principal().amount())
        .param("indexPercentage", f.indexPercentage())
        .param("exempt", f.incomeTaxExempt())
        .param("start", f.start())
        .update();
  }

  private void insert(int seq, Contribution c) {
    jdbc.sql(
            """
            insert into portfolio_contribution (portfolio_id, seq, amount, made_at)
            values (:id, :seq, :amount, :at)
            """)
        .param("id", PORTFOLIO_ID)
        .param("seq", seq)
        .param("amount", c.amount().amount())
        .param("at", timestamp(c.at()))
        .update();
  }

  private static Position position(ResultSet rs) throws SQLException {
    return new Position(
        Ticker.of(rs.getString("ticker")),
        rs.getString("name"),
        AssetClass.valueOf(rs.getString("asset_class")),
        rs.getLong("quantity"),
        money(rs, "average_price"),
        money(rs, "realized_pnl"));
  }

  private static Execution execution(ResultSet rs) throws SQLException {
    return new Execution(
        rs.getLong("id"),
        OrderSide.valueOf(rs.getString("side")),
        Ticker.of(rs.getString("ticker")),
        rs.getLong("quantity"),
        money(rs, "price"),
        money(rs, "fees"),
        money(rs, "total"),
        instant(rs, "executed_at"));
  }

  private static FixedIncomeHolding fixedIncome(ResultSet rs) throws SQLException {
    return new FixedIncomeHolding(
        rs.getLong("id"),
        rs.getString("product_id"),
        rs.getString("name"),
        money(rs, "principal"),
        percentage(rs.getBigDecimal("index_percentage")),
        rs.getBoolean("income_tax_exempt"),
        rs.getObject("start_date", LocalDate.class));
  }

  private static Contribution contribution(ResultSet rs) throws SQLException {
    return new Contribution(money(rs, "amount"), instant(rs, "made_at"));
  }

  // A coluna guarda 110.0000; o domínio recebeu 110, e os dois precisam ser iguais.
  private static BigDecimal percentage(BigDecimal stored) {
    BigDecimal plain = stored.stripTrailingZeros();
    return plain.scale() < 0 ? plain.setScale(0) : plain;
  }

  private static Money money(ResultSet rs, String column) throws SQLException {
    return Money.brl(rs.getBigDecimal(column));
  }

  private static Instant instant(ResultSet rs, String column) throws SQLException {
    return rs.getObject(column, OffsetDateTime.class).toInstant();
  }

  private static OffsetDateTime timestamp(Instant at) {
    return at.atOffset(ZoneOffset.UTC);
  }

  @FunctionalInterface
  private interface RowReader<T> {
    T read(ResultSet rs) throws SQLException;
  }
}
