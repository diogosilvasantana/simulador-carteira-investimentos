package br.com.simuladorcarteira.portfolio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.simuladorcarteira.PostgresTestConfiguration;
import br.com.simuladorcarteira.portfolio.application.PortfolioRepository;
import br.com.simuladorcarteira.portfolio.domain.AssetClass;
import br.com.simuladorcarteira.portfolio.domain.InsufficientCashException;
import br.com.simuladorcarteira.portfolio.domain.PortfolioState;
import br.com.simuladorcarteira.portfolio.domain.TradingCosts;
import br.com.simuladorcarteira.shared.Money;
import br.com.simuladorcarteira.shared.Ticker;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = "simulador.fixedincome.sync.enabled=false")
@Import(PostgresTestConfiguration.class)
class PortfolioPersistenceIntegrationTest {

  private static final Instant NOW = Instant.parse("2026-11-25T15:00:00Z");
  private static final Ticker PETR4 = new Ticker("PETR4");
  private static final TradingCosts COSTS =
      new TradingCosts(
          List.of(new TradingCosts.Rate(LocalDate.of(2019, 1, 1), new BigDecimal("0.03"))));

  @Autowired private PortfolioRepository repository;

  @BeforeEach
  void startFromScratch() {
    repository.reset();
  }

  @Test
  void shouldReadBackEverythingThatWasSaved() {
    PortfolioState saved =
        repository.update(
            p -> {
              p.contribute(Money.brl("10000.00"), NOW);
              p.buy(PETR4, "PETROBRAS PN", AssetClass.STOCKS, 100, Money.brl("38.00"), COSTS, NOW);
              p.sell(PETR4, 40, Money.brl("40.00"), COSTS, NOW);
              p.investFixedIncome(
                  "cdb-110",
                  "CDB 110% do CDI",
                  Money.brl("1000.00"),
                  new BigDecimal("110"),
                  false,
                  NOW);
              return p.state();
            });

    PortfolioState read = repository.update(p -> p.state());

    assertThat(read).isEqualTo(saved);
  }

  @Test
  void shouldKeepTheBalanceWhenAnOrderIsRejected() {
    repository.update(p -> p.contribute(Money.brl("100.00"), NOW));

    assertThatThrownBy(
            () ->
                repository.update(
                    p ->
                        p.buy(
                            PETR4,
                            "PETROBRAS PN",
                            AssetClass.STOCKS,
                            100,
                            Money.brl("38.00"),
                            COSTS,
                            NOW)))
        .isInstanceOf(InsufficientCashException.class);

    Money cash = repository.update(p -> p.cash());

    assertThat(cash).isEqualTo(Money.brl("100.00"));
  }

  @Test
  void shouldStartOverAfterReset() {
    repository.update(p -> p.contribute(Money.brl("100.00"), NOW));

    repository.reset();

    PortfolioState state = repository.update(p -> p.state());

    assertThat(state).isEqualTo(PortfolioState.empty());
  }
}
