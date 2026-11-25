package br.com.simuladorcarteira.fixedincome;

import java.math.BigDecimal;

/** Produto de renda fixa visto de fora do módulo (API pública, ADR-004). */
public record FixedIncomeOffer(
    String id,
    String name,
    BigDecimal indexPercentage,
    boolean incomeTaxExempt,
    int gracePeriodDays) {}
