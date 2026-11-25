package br.com.simuladorcarteira.portfolio.domain;

import br.com.simuladorcarteira.shared.Money;
import br.com.simuladorcarteira.shared.Ticker;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A carteira simulada: caixa, aportes, posições em renda variável, aplicações em renda fixa e o
 * histórico de execuções. Todas as regras de dinheiro moram aqui; a aplicação só busca preços.
 */
public final class Portfolio {

  private static final ZoneId B3 = ZoneId.of("America/Sao_Paulo");

  private Money cash = Money.zero();
  private Money contributed = Money.zero();
  private final Map<Ticker, Position> positions = new LinkedHashMap<>();
  private final List<FixedIncomeHolding> fixedIncome = new ArrayList<>();
  private final List<Contribution> contributions = new ArrayList<>();
  private final List<Execution> executions = new ArrayList<>();
  private long sequence;

  public Money cash() {
    return cash;
  }

  /** Soma dos aportes: a base para medir quanto a carteira rendeu. */
  public Money contributed() {
    return contributed;
  }

  public Collection<Position> positions() {
    return positions.values().stream().filter(p -> p.quantity() > 0).toList();
  }

  public Optional<Position> position(Ticker ticker) {
    return Optional.ofNullable(positions.get(ticker)).filter(p -> p.quantity() > 0);
  }

  /** Resultado já realizado nas vendas, inclusive de posições zeradas. */
  public Money realizedPnl() {
    return positions.values().stream().map(Position::realizedPnl).reduce(Money.zero(), Money::plus);
  }

  public List<FixedIncomeHolding> fixedIncome() {
    return List.copyOf(fixedIncome);
  }

  public List<Contribution> contributions() {
    return List.copyOf(contributions);
  }

  public List<Execution> executions() {
    return List.copyOf(executions);
  }

  public Contribution contribute(Money amount, Instant at) {
    Contribution contribution = new Contribution(amount, at);
    contributions.add(contribution);
    cash = cash.plus(amount);
    contributed = contributed.plus(amount);
    return contribution;
  }

  /** Quanto a ordem custaria ou renderia agora, sem executar: base da etapa de revisão. */
  public OrderQuote quote(
      OrderSide side, Ticker ticker, long quantity, Money price, TradingCosts costs, Instant at) {
    requirePositive(quantity);
    Money gross = price.times(quantity);
    Money fees = costs.feeOn(LocalDate.ofInstant(at, B3), gross);
    Money total = side == OrderSide.BUY ? gross.plus(fees) : gross.minus(fees);
    return new OrderQuote(side, ticker, quantity, price, gross, fees, total);
  }

  public Execution buy(
      Ticker ticker,
      String name,
      AssetClass assetClass,
      long quantity,
      Money price,
      TradingCosts costs,
      Instant at) {
    OrderQuote quote = quote(OrderSide.BUY, ticker, quantity, price, costs, at);
    if (quote.total().isGreaterThan(cash)) {
      throw new InsufficientCashException(quote.total(), cash);
    }
    cash = cash.minus(quote.total());
    positions.put(
        ticker,
        positions
            .getOrDefault(ticker, Position.open(ticker, name, assetClass))
            .buy(quantity, quote.total()));
    return record(quote, at);
  }

  public Execution sell(Ticker ticker, long quantity, Money price, TradingCosts costs, Instant at) {
    OrderQuote quote = quote(OrderSide.SELL, ticker, quantity, price, costs, at);
    long available = position(ticker).map(Position::quantity).orElse(0L);
    if (quantity > available) {
      throw new InsufficientPositionException(ticker, quantity, available);
    }
    cash = cash.plus(quote.total());
    positions.put(ticker, positions.get(ticker).sell(quantity, quote.total()));
    return record(quote, at);
  }

  public FixedIncomeHolding investFixedIncome(
      String productId,
      String name,
      Money amount,
      BigDecimal indexPercentage,
      boolean incomeTaxExempt,
      Instant at) {
    Objects.requireNonNull(amount, "amount");
    if (amount.amount().signum() <= 0) {
      throw new IllegalArgumentException("A aplicação precisa ser positiva");
    }
    if (amount.isGreaterThan(cash)) {
      throw new InsufficientCashException(amount, cash);
    }
    cash = cash.minus(amount);
    FixedIncomeHolding holding =
        new FixedIncomeHolding(
            ++sequence,
            productId,
            name,
            amount,
            indexPercentage,
            incomeTaxExempt,
            LocalDate.ofInstant(at, B3));
    fixedIncome.add(holding);
    return holding;
  }

  private Execution record(OrderQuote quote, Instant at) {
    Execution execution =
        new Execution(
            ++sequence,
            quote.side(),
            quote.ticker(),
            quote.quantity(),
            quote.price(),
            quote.fees(),
            quote.total(),
            at);
    executions.add(execution);
    return execution;
  }

  private static void requirePositive(long quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException("A quantidade precisa ser positiva");
    }
  }
}
