package br.com.simuladorcarteira.fixedincome.adapters.in.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

/**
 * Pedido de simulação de um pós-fixado: "R$ 10.000 a 105% do CDI de 02/01/2026 até 24/11/2026".
 * Datas no futuro são projetadas com o último CDI publicado.
 */
record SimulationRequest(
    @NotNull @Positive @Nullable BigDecimal principal,
    @NotNull @Positive @Nullable BigDecimal indexPercentage,
    @NotNull @Nullable LocalDate start,
    @NotNull @Nullable LocalDate end,
    boolean incomeTaxExempt) {}
