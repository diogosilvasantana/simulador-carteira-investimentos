package br.com.simuladorcarteira;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.simple.JdbcClient;

/** O dev.cmd usa este perfil: precisa subir sem Docker e guardar os dados entre execuções. */
class LocalProfileTests {

  @TempDir private Path dir;

  @Test
  void shouldKeepDataBetweenRunsWithoutDocker() {
    try (ConfigurableApplicationContext first = start()) {
      first
          .getBean(JdbcClient.class)
          .sql("insert into budget_item (name, kind, amount) values ('Salário', 'INCOME', 10)")
          .update();
    }

    try (ConfigurableApplicationContext second = start()) {
      long items =
          second
              .getBean(JdbcClient.class)
              .sql("select count(*) from budget_item")
              .query(Long.class)
              .single();

      assertThat(items).isEqualTo(1);
    }
  }

  @Test
  void shouldListenOnlyOnThisComputer() {
    try (ConfigurableApplicationContext context = start()) {
      assertThat(context.getEnvironment().getProperty("server.address")).isEqualTo("127.0.0.1");
    }
  }

  private ConfigurableApplicationContext start() {
    return new SpringApplicationBuilder(SimuladorCarteiraApplication.class)
        .web(WebApplicationType.SERVLET)
        .profiles("local")
        .run(
            "--server.port=0",
            "--simulador.fixedincome.sync.enabled=false",
            "--simulador.local.data-dir=" + dir);
  }
}
