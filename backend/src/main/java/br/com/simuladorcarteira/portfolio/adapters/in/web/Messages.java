package br.com.simuladorcarteira.portfolio.adapters.in.web;

import br.com.simuladorcarteira.portfolio.application.PortfolioSummary;
import br.com.simuladorcarteira.portfolio.domain.Execution;
import br.com.simuladorcarteira.portfolio.domain.OrderQuote;
import br.com.simuladorcarteira.shared.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.jspecify.annotations.Nullable;

/** Respostas da API: dinheiro e percentuais em texto decimal, como no resto da API. */
final class Messages {

  private Messages() {}

  static String money(Money value) {
    return value.amount().toPlainString();
  }

  static @Nullable String money(@Nullable Money value) {
    return value == null ? null : value.amount().toPlainString();
  }

  static @Nullable String decimal(@Nullable BigDecimal value) {
    return value == null ? null : value.toPlainString();
  }

  record SummaryMessage(
      String total,
      String cash,
      String contributed,
      String result,
      @Nullable String resultPercent,
      String unrealizedPnl,
      String realizedPnl,
      List<AllocationMessage> allocation,
      List<PositionMessage> positions,
      List<FixedIncomeMessage> fixedIncome,
      List<ExecutionMessage> recentExecutions) {

    static SummaryMessage from(PortfolioSummary s) {
      return new SummaryMessage(
          money(s.total()),
          money(s.cash()),
          money(s.contributed()),
          money(s.result()),
          decimal(s.resultPercent()),
          money(s.unrealizedPnl()),
          money(s.realizedPnl()),
          s.allocation().stream()
              .map(
                  a ->
                      new AllocationMessage(
                          a.assetClass().slug(), money(a.value()), a.percent().toPlainString()))
              .toList(),
          s.positions().stream().map(PositionMessage::from).toList(),
          s.fixedIncome().stream().map(FixedIncomeMessage::from).toList(),
          s.recentExecutions().stream().map(ExecutionMessage::from).toList());
    }
  }

  record AllocationMessage(String assetClass, String value, String percent) {}

  record PositionMessage(
      String ticker,
      String name,
      String assetClass,
      long quantity,
      String averagePrice,
      @Nullable String lastPrice,
      @Nullable String changePercent,
      String marketValue,
      String unrealizedPnl,
      @Nullable String unrealizedPnlPercent,
      String weightPercent) {

    static PositionMessage from(PortfolioSummary.PositionView p) {
      return new PositionMessage(
          p.ticker().value(),
          p.name(),
          p.assetClass().slug(),
          p.quantity(),
          money(p.averagePrice()),
          money(p.lastPrice()),
          decimal(p.changePercent()),
          money(p.marketValue()),
          money(p.unrealizedPnl()),
          decimal(p.unrealizedPnlPercent()),
          p.weightPercent().toPlainString());
    }
  }

  record FixedIncomeMessage(
      long id,
      String productId,
      String name,
      String principal,
      String gross,
      String net,
      LocalDate start,
      boolean ratesAvailable) {

    static FixedIncomeMessage from(PortfolioSummary.FixedIncomeView f) {
      return new FixedIncomeMessage(
          f.id(),
          f.productId(),
          f.name(),
          money(f.principal()),
          money(f.gross()),
          money(f.net()),
          f.start(),
          f.ratesAvailable());
    }
  }

  record ExecutionMessage(
      long id,
      String side,
      String ticker,
      long quantity,
      String price,
      String fees,
      String total,
      Instant executedAt) {

    static ExecutionMessage from(Execution e) {
      return new ExecutionMessage(
          e.id(),
          e.side().name(),
          e.ticker().value(),
          e.quantity(),
          money(e.price()),
          money(e.fees()),
          money(e.total()),
          e.executedAt());
    }
  }

  record QuoteMessage(
      String side,
      String ticker,
      long quantity,
      String price,
      String gross,
      String fees,
      String total) {

    static QuoteMessage from(OrderQuote q) {
      return new QuoteMessage(
          q.side().name(),
          q.ticker().value(),
          q.quantity(),
          money(q.price()),
          money(q.gross()),
          money(q.fees()),
          money(q.total()));
    }
  }
}
