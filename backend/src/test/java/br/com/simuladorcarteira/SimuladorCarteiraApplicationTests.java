package br.com.simuladorcarteira;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
class SimuladorCarteiraApplicationTests {

  @Test
  void contextLoads() {
    // Falha se a configuração do Spring estiver quebrada.
  }
}
