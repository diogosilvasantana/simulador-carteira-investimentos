package br.com.simuladorcarteira.fixedincome.adapters.in.schedule;

import br.com.simuladorcarteira.fixedincome.application.SyncIndexRates;
import java.time.LocalDate;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Atualiza os indexadores na subida e duas vezes por dia: o Banco Central publica o CDI do dia
 * anterior pela manhã e a Selic depois das reuniões do Copom.
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(SyncProperties.class)
@ConditionalOnProperty(name = "simulador.fixedincome.sync.enabled", matchIfMissing = true)
class IndexRateSyncJob {

  private static final Logger log = LoggerFactory.getLogger(IndexRateSyncJob.class);
  private static final ZoneId B3 = ZoneId.of("America/Sao_Paulo");

  private final SyncIndexRates syncIndexRates;
  private final SyncProperties properties;

  IndexRateSyncJob(SyncIndexRates syncIndexRates, SyncProperties properties) {
    this.syncIndexRates = syncIndexRates;
    this.properties = properties;
  }

  @EventListener(ApplicationReadyEvent.class)
  void onStartup() {
    // Numa thread à parte: sem internet, a aplicação sobe do mesmo jeito.
    Thread.ofVirtual().name("index-rate-sync").start(this::sync);
  }

  @Scheduled(cron = "${simulador.fixedincome.sync.cron:0 0 8,20 * * *}", zone = "America/Sao_Paulo")
  void sync() {
    LocalDate today = LocalDate.now(B3);
    int saved = syncIndexRates.sync(today.minusYears(properties.backfillYears()), today);
    log.info("Indexadores atualizados: {} taxas novas", saved);
  }
}
