package br.com.simuladorcarteira;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "simulador.fixedincome.sync.enabled=false")
@ActiveProfiles("local")
class LocalProfileTests {

  @Test
  void shouldStartWithoutADatabase(@Autowired ObjectProvider<DataSource> dataSource) {
    assertThat(dataSource.getIfAvailable()).isNull();
  }
}
