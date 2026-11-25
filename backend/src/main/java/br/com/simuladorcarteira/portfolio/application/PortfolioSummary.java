package br.com.simuladorcarteira.portfolio.application;

import br.com.simuladorcarteira.portfolio.domain.AssetClass;
import br.com.simuladorcarteira.portfolio.domain.Execution;
import br.com.simuladorcarteira.shared.Money;
import br.com.simuladorcarteira.shared.Ticker;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.jspecify.annotations.Nullable;

/** Fotografia da carteira avaliada a preço de mercado, pronta para a tela. */
public record PortfolioSummary(
    Money total,
    Money cash,
    Money contributed,
    Money result,
    @Nullable BigDecimal resultPercent,
    Money unrealizedPnl,
    Money realizedPnl,
    List<Allocation> allocation,
    List<PositionView> positions,
    List<FixedIncomeView> fixedIncome,
    List<Execution> recentExecutions) {

  /** Peso de uma classe no patrimônio investido (sem o caixa). */
  public record Allocation(AssetClass assetClass, Money value, BigDecimal percent) {}

  public record PositionView(
      Ticker ticker,
      String name,
      AssetClass assetClass,
      long quantity,
      Money averagePrice,
      @Nullable Money lastPrice,
      @Nullable BigDecimal changePercent,
      Money marketValue,
      Money unrealizedPnl,
      @Nullable BigDecimal unrealizedPnlPercent,
      BigDecimal weightPercent) {}

  public record FixedIncomeView(
      long id,
      String productId,
      String name,
      Money principal,
      Money gross,
      Money net,
      LocalDate start,
      boolean ratesAvailable) {}
}
