package br.com.simuladorcarteira.fixedincome.application;

import br.com.simuladorcarteira.fixedincome.domain.RedemptionResult;
import java.time.LocalDate;

/**
 * Simulação de resgate. Dias úteis depois da última taxa publicada são projetados com essa última
 * taxa; {@code projectedBusinessDays} diz quantos foram.
 */
public record RedemptionSimulation(
    RedemptionResult result, LocalDate ratesUntil, int projectedBusinessDays) {}
