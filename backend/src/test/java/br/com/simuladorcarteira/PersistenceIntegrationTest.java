package br.com.simuladorcarteira;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(properties = "simulador.fixedincome.sync.enabled=false")
@Import({PostgresTestConfiguration.class, PersistenceIntegrationTest.FailingListener.class})
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class PersistenceIntegrationTest {

  private final JdbcClient jdbc;
  private final ApplicationEventPublisher events;
  private final TransactionTemplate transaction;

  @Autowired
  PersistenceIntegrationTest(
      JdbcClient jdbc, ApplicationEventPublisher events, TransactionTemplate transaction) {
    this.jdbc = jdbc;
    this.events = events;
    this.transaction = transaction;
  }

  @Test
  void shouldApplyTheBaselineMigrationOnStartup() {
    boolean applied =
        jdbc.sql("select success from flyway_schema_history where version = '1'")
            .query(Boolean.class)
            .single();

    assertThat(applied).isTrue();
  }

  @Test
  void shouldKeepTheEventWhenItsListenerFails() {
    transaction.executeWithoutResult(status -> events.publishEvent(new ProbeEvent("keep-me")));

    long incomplete =
        jdbc.sql(
                """
                select count(*) from event_publication
                where serialized_event like '%keep-me%' and completion_date is null
                """)
            .query(Long.class)
            .single();

    assertThat(incomplete).isEqualTo(1);
  }

  @Test
  void shouldNotKeepTheEventWhenTheTransactionRollsBack() {
    transaction.executeWithoutResult(
        status -> {
          events.publishEvent(new ProbeEvent("roll-me-back"));
          status.setRollbackOnly();
        });

    long stored =
        jdbc.sql("select count(*) from event_publication where serialized_event like :event")
            .param("event", "%roll-me-back%")
            .query(Long.class)
            .single();

    assertThat(stored).isZero();
  }

  record ProbeEvent(String id) {}

  static class FailingListener {

    @ApplicationModuleListener
    void on(ProbeEvent event) {
      throw new IllegalStateException("Falha proposital: " + event.id());
    }
  }
}
