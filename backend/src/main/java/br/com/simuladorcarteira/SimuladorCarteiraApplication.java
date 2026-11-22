package br.com.simuladorcarteira;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Ponto de entrada do monólito modular. Cada subpacote direto é um módulo de negócio. */
@SpringBootApplication
public class SimuladorCarteiraApplication {

  public static void main(String[] args) {
    SpringApplication.run(SimuladorCarteiraApplication.class, args);
  }
}
