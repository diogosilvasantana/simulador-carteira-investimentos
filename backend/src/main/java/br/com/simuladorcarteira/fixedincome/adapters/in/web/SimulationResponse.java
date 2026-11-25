package br.com.simuladorcarteira.fixedincome.adapters.in.web;

import br.com.simuladorcarteira.fixedincome.application.RedemptionSimulation;
import br.com.simuladorcarteira.fixedincome.domain.RedemptionResult;
import java.time.LocalDate;

/** Resultado da simulação; valores em texto decimal, como no resto da API. */
record SimulationResponse(
    String principal,
    String gross,
    String grossYield,
    String iof,
    String incomeTax,
    String net,
    long holdingDays,
    String iofRatePercent,
    String incomeTaxRatePercent,
    LocalDate ratesUntil,
    int projectedBusinessDays) {

  static SimulationResponse from(RedemptionSimulation simulation) {
    RedemptionResult r = simulation.result();
    return new SimulationResponse(
        r.principal().amount().toPlainString(),
        r.gross().amount().toPlainString(),
        r.grossYield().amount().toPlainString(),
        r.iof().amount().toPlainString(),
        r.incomeTax().amount().toPlainString(),
        r.net().amount().toPlainString(),
        r.holdingDays(),
        r.iofRatePercent().toPlainString(),
        r.incomeTaxRatePercent().toPlainString(),
        simulation.ratesUntil(),
        simulation.projectedBusinessDays());
  }
}
