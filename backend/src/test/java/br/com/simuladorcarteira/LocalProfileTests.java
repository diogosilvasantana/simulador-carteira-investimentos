package br.com.simuladorcarteira;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;

// Banco em memória para o teste não criar arquivo; o dialeto e as migrations são os do perfil
// local.
@SpringBootTest(
    properties = {
      "simulador.fixedincome.sync.enabled=false",
      "spring.datasource.url=jdbc:h2:mem:local;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
    })
@ActiveProfiles("local")
class LocalProfileTests {

  @Test
  void shouldMigrateTheEmbeddedDatabaseWithoutDocker(@Autowired JdbcClient jdbc) {
    long migrations =
        jdbc.sql("select count(*) from flyway_schema_history where success")
            .query(Long.class)
            .single();

    assertThat(migrations).isGreaterThanOrEqualTo(2);
  }
}
