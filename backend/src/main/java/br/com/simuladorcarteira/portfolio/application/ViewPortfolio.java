package br.com.simuladorcarteira.portfolio.application;

import br.com.simuladorcarteira.fixedincome.FixedIncomeMarket;
import br.com.simuladorcarteira.fixedincome.FixedIncomeValue;
import br.com.simuladorcarteira.marketdata.MarketQuote;
import br.com.simuladorcarteira.marketdata.MarketQuotes;
import br.com.simuladorcarteira.portfolio.application.PortfolioSummary.Allocation;
import br.com.simuladorcarteira.portfolio.application.PortfolioSummary.FixedIncomeView;
import br.com.simuladorcarteira.portfolio.application.PortfolioSummary.PositionView;
import br.com.simuladorcarteira.portfolio.domain.AssetClass;
import br.com.simuladorcarteira.portfolio.domain.Execution;
import br.com.simuladorcarteira.portfolio.domain.FixedIncomeHolding;
import br.com.simuladorcarteira.portfolio.domain.Portfolio;
import br.com.simuladorcarteira.portfolio.domain.Position;
import br.com.simuladorcarteira.shared.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

/** Avalia a carteira a preço de mercado: posições pelo último preço, renda fixa pelo CDI. */
@Service
public class ViewPortfolio {

  private static final ZoneId B3 = ZoneId.of("America/Sao_Paulo");
  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
  private static final int RECENT = 10;

  private final PortfolioRepository repository;
  private final MarketQuotes quotes;
  private final FixedIncomeMarket fixedIncome;
  private final Clock clock;

  public ViewPortfolio(
      PortfolioRepository repository,
      MarketQuotes quotes,
      FixedIncomeMarket fixedIncome,
      Clock clock) {
    this.repository = repository;
    this.quotes = quotes;
    this.fixedIncome = fixedIncome;
    this.clock = clock;
  }

  public PortfolioSummary summary() {
    Snapshot snapshot = repository.update(Snapshot::of);
    LocalDate today = LocalDate.now(clock.withZone(B3));

    List<Valued> valued = new ArrayList<>();
    for (Position position : snapshot.positions()) {
      MarketQuote quote = quotes.find(position.ticker()).orElse(null);
      @Nullable Money last = quote == null ? null : quote.last();
      Money value = last == null ? position.invested() : last.times(position.quantity());
      valued.add(new Valued(position, last, quote == null ? null : quote.changePercent(), value));
    }
    List<FixedIncomeView> fixed = new ArrayList<>();
    for (FixedIncomeHolding holding : snapshot.fixedIncome()) {
      FixedIncomeValue value =
          fixedIncome.valueOf(
              holding.principal(),
              holding.indexPercentage(),
              holding.start(),
              today,
              holding.incomeTaxExempt());
      fixed.add(
          new FixedIncomeView(
              holding.id(),
              holding.productId(),
              holding.name(),
              holding.principal(),
              value.gross(),
              value.net(),
              holding.start(),
              value.ratesAvailable()));
    }

    Money invested =
        valued.stream()
            .map(Valued::value)
            .reduce(Money.zero(), Money::plus)
            .plus(fixed.stream().map(FixedIncomeView::gross).reduce(Money.zero(), Money::plus));
    Money total = snapshot.cash().plus(invested);
    Money unrealized =
        valued.stream()
            .map(v -> v.value().minus(v.position().invested()))
            .reduce(Money.zero(), Money::plus)
            .plus(
                fixed.stream()
                    .map(f -> f.gross().minus(f.principal()))
                    .reduce(Money.zero(), Money::plus));
    Money result = total.minus(snapshot.contributed());

    Map<AssetClass, Money> byClass = new EnumMap<>(AssetClass.class);
    valued.forEach(v -> byClass.merge(v.position().assetClass(), v.value(), Money::plus));
    fixed.forEach(f -> byClass.merge(AssetClass.FIXED_INCOME, f.gross(), Money::plus));
    List<Allocation> allocation =
        byClass.entrySet().stream()
            .map(e -> new Allocation(e.getKey(), e.getValue(), percentOf(e.getValue(), invested)))
            .toList();

    List<PositionView> positions =
        valued.stream()
            .map(
                v -> {
                  Position p = v.position();
                  Money pnl = v.value().minus(p.invested());
                  return new PositionView(
                      p.ticker(),
                      p.name(),
                      p.assetClass(),
                      p.quantity(),
                      p.averagePrice(),
                      v.last(),
                      v.changePercent(),
                      v.value(),
                      pnl,
                      percentOrNull(pnl, p.invested()),
                      percentOf(v.value(), invested));
                })
            .toList();

    List<Execution> executions = snapshot.executions();
    List<Execution> recent =
        executions.subList(Math.max(0, executions.size() - RECENT), executions.size()).reversed();

    return new PortfolioSummary(
        total,
        snapshot.cash(),
        snapshot.contributed(),
        result,
        percentOrNull(result, snapshot.contributed()),
        unrealized,
        snapshot.realizedPnl(),
        allocation,
        positions,
        fixed,
        recent);
  }

  private static BigDecimal percentOf(Money part, Money whole) {
    BigDecimal value = percentOrNull(part, whole);
    return value == null ? BigDecimal.ZERO.setScale(2) : value;
  }

  private static @Nullable BigDecimal percentOrNull(Money part, Money whole) {
    if (whole.amount().signum() == 0) {
      return null;
    }
    return part.amount().multiply(HUNDRED).divide(whole.amount(), 2, RoundingMode.HALF_EVEN);
  }

  private record Valued(
      Position position, @Nullable Money last, @Nullable BigDecimal changePercent, Money value) {}

  /** Cópia lida dentro do bloqueio, para avaliar a preço de mercado sem segurar a carteira. */
  private record Snapshot(
      Money cash,
      Money contributed,
      Money realizedPnl,
      List<Position> positions,
      List<FixedIncomeHolding> fixedIncome,
      List<Execution> executions) {

    static Snapshot of(Portfolio portfolio) {
      return new Snapshot(
          portfolio.cash(),
          portfolio.contributed(),
          portfolio.realizedPnl(),
          List.copyOf(portfolio.positions()),
          portfolio.fixedIncome(),
          portfolio.executions());
    }
  }
}
