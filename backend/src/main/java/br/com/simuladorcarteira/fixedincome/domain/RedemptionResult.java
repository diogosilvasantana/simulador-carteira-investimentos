package br.com.simuladorcarteira.fixedincome.domain;

import br.com.simuladorcarteira.shared.Money;
import java.math.BigDecimal;

/** Resultado de um resgate: o que rendeu, os impostos e o que sobra para o investidor. */
public record RedemptionResult(
    Money principal,
    Money gross,
    Money grossYield,
    Money iof,
    Money incomeTax,
    Money net,
    long holdingDays,
    BigDecimal iofRatePercent,
    BigDecimal incomeTaxRatePercent) {}
