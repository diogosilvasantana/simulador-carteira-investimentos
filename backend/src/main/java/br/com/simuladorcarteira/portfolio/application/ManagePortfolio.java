package br.com.simuladorcarteira.portfolio.application;

import br.com.simuladorcarteira.fixedincome.FixedIncomeMarket;
import br.com.simuladorcarteira.fixedincome.FixedIncomeOffer;
import br.com.simuladorcarteira.marketdata.MarketQuote;
import br.com.simuladorcarteira.marketdata.MarketQuotes;
import br.com.simuladorcarteira.portfolio.domain.AssetClass;
import br.com.simuladorcarteira.portfolio.domain.Contribution;
import br.com.simuladorcarteira.portfolio.domain.Execution;
import br.com.simuladorcarteira.portfolio.domain.FixedIncomeHolding;
import br.com.simuladorcarteira.portfolio.domain.OrderQuote;
import br.com.simuladorcarteira.portfolio.domain.OrderSide;
import br.com.simuladorcarteira.portfolio.domain.TradingCosts;
import br.com.simuladorcarteira.shared.Money;
import br.com.simuladorcarteira.shared.Ticker;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;

/** Casos de uso que mexem na carteira: aporte, ordem a mercado e aplicação em renda fixa. */
@Service
public class ManagePortfolio {

  private final PortfolioRepository repository;
  private final MarketQuotes quotes;
  private final FixedIncomeMarket fixedIncome;
  private final TradingCosts costs;
  private final Clock clock;

  public ManagePortfolio(
      PortfolioRepository repository,
      MarketQuotes quotes,
      FixedIncomeMarket fixedIncome,
      TradingCosts costs,
      Clock clock) {
    this.repository = repository;
    this.quotes = quotes;
    this.fixedIncome = fixedIncome;
    this.costs = costs;
    this.clock = clock;
  }

  public Contribution contribute(Money amount) {
    Instant now = clock.instant();
    return repository.update(portfolio -> portfolio.contribute(amount, now));
  }

  /** Prévia da ordem com o preço de agora, para a etapa de revisão. */
  public OrderQuote preview(OrderSide side, Ticker ticker, long quantity) {
    MarketQuote quote = priced(ticker);
    Instant now = clock.instant();
    return repository.update(
        portfolio -> portfolio.quote(side, ticker, quantity, lastOf(quote), costs, now));
  }

  /** Ordem a mercado: executa no último preço recebido do MT5. */
  public Execution execute(OrderSide side, Ticker ticker, long quantity) {
    MarketQuote quote = priced(ticker);
    Money price = lastOf(quote);
    Instant now = clock.instant();
    return repository.update(
        portfolio ->
            side == OrderSide.BUY
                ? portfolio.buy(ticker, quote.name(), classOf(quote), quantity, price, costs, now)
                : portfolio.sell(ticker, quantity, price, costs, now));
  }

  public FixedIncomeHolding investFixedIncome(String productId, Money amount) {
    FixedIncomeOffer offer =
        fixedIncome
            .offer(productId)
            .orElseThrow(() -> new PortfolioErrors.UnknownProductException(productId));
    Instant now = clock.instant();
    return repository.update(
        portfolio ->
            portfolio.investFixedIncome(
                offer.id(),
                offer.name(),
                amount,
                offer.indexPercentage(),
                offer.incomeTaxExempt(),
                now));
  }

  /** Recomeça o treino do zero. */
  public void reset() {
    repository.reset();
  }

  private MarketQuote priced(Ticker ticker) {
    MarketQuote quote =
        quotes
            .find(ticker)
            .orElseThrow(() -> new PortfolioErrors.UnknownProductException(ticker.value()));
    if (quote.last() == null) {
      throw new PortfolioErrors.NoQuoteException(ticker);
    }
    return quote;
  }

  private static Money lastOf(MarketQuote quote) {
    Money last = quote.last();
    if (last == null) {
      throw new PortfolioErrors.NoQuoteException(quote.ticker());
    }
    return last;
  }

  // Ativo sem categoria conhecida (classificação não reconheceu o código) conta como ação.
  private static AssetClass classOf(MarketQuote quote) {
    String slug = quote.categorySlug();
    return slug == null ? AssetClass.STOCKS : AssetClass.fromSlug(slug);
  }
}
