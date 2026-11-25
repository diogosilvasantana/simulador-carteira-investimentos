package br.com.simuladorcarteira.fixedincome.application;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.simuladorcarteira.fixedincome.domain.IndexRate;
import br.com.simuladorcarteira.fixedincome.domain.RateIndex;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SyncIndexRatesTest {

  private static final LocalDate TODAY = LocalDate.of(2026, 11, 24);
  private static final LocalDate BACKFILL = LocalDate.of(2023, 11, 24);

  private final FakeRates rates = new FakeRates();
  private final List<String> requests = new ArrayList<>();

  @Test
  void shouldBackfillEveryIndexOnTheFirstRun() {
    SyncIndexRates sync = new SyncIndexRates(rates, recordingSource(false));

    int saved = sync.sync(BACKFILL, TODAY);

    assertThat(saved).isEqualTo(3);
    assertThat(requests)
        .containsExactlyInAnyOrder(
            "CDI 2023-11-24..2026-11-24",
            "SELIC_TARGET 2023-11-24..2026-11-24",
            "IPCA 2023-11-24..2026-11-24");
  }

  @Test
  void shouldAskOnlyForDaysAfterTheLastStoredRate() {
    rates.saveAll(List.of(new IndexRate(RateIndex.CDI, TODAY.minusDays(3), BigDecimal.ONE)));
    SyncIndexRates sync = new SyncIndexRates(rates, recordingSource(false));

    sync.sync(BACKFILL, TODAY);

    assertThat(requests).contains("CDI 2026-11-22..2026-11-24");
  }

  @Test
  void shouldKeepSyncingOtherIndexesWhenOneFails() {
    SyncIndexRates sync = new SyncIndexRates(rates, recordingSource(true));

    int saved = sync.sync(BACKFILL, TODAY);

    assertThat(saved).isEqualTo(2);
    assertThat(rates.latest(RateIndex.IPCA)).isPresent();
  }

  private IndexRateSource recordingSource(boolean cdiFails) {
    return (index, from, to) -> {
      requests.add(index + " " + from + ".." + to);
      if (cdiFails && index == RateIndex.CDI) {
        throw new IllegalStateException("Banco Central fora do ar");
      }
      return List.of(new IndexRate(index, to, BigDecimal.ONE));
    };
  }
}
