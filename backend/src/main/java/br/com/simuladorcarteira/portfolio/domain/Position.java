package br.com.simuladorcarteira.portfolio.domain;

import br.com.simuladorcarteira.shared.Money;
import br.com.simuladorcarteira.shared.Ticker;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Posição em renda variável. O preço médio inclui os custos da compra, como manda a Receita para
 * apurar o ganho de capital; a venda não muda o preço médio, só realiza o resultado.
 */
public record Position(
    Ticker ticker,
    String name,
    AssetClass assetClass,
    long quantity,
    Money averagePrice,
    Money realizedPnl) {

  public Position {
    Objects.requireNonNull(ticker, "ticker");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(assetClass, "assetClass");
    Objects.requireNonNull(averagePrice, "averagePrice");
    Objects.requireNonNull(realizedPnl, "realizedPnl");
    if (quantity < 0) {
      throw new IllegalArgumentException("Quantidade negativa em " + ticker);
    }
  }

  static Position open(Ticker ticker, String name, AssetClass assetClass) {
    return new Position(ticker, name, assetClass, 0, Money.zero(), Money.zero());
  }

  Position buy(long quantity, Money totalCost) {
    long newQuantity = this.quantity + quantity;
    Money invested = averagePrice.times(this.quantity).plus(totalCost);
    Money newAverage =
        Money.brl(
            invested.amount().divide(BigDecimal.valueOf(newQuantity), 2, RoundingMode.HALF_EVEN));
    return new Position(ticker, name, assetClass, newQuantity, newAverage, realizedPnl);
  }

  Position sell(long quantity, Money netProceeds) {
    Money cost = averagePrice.times(quantity);
    return new Position(
        ticker,
        name,
        assetClass,
        this.quantity - quantity,
        this.quantity == quantity ? Money.zero() : averagePrice,
        realizedPnl.plus(netProceeds.minus(cost)));
  }

  public Money invested() {
    return averagePrice.times(quantity);
  }
}
