package br.com.simuladorcarteira.portfolio.adapters.in.web;

import br.com.simuladorcarteira.portfolio.domain.OrderSide;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

/** Corpos aceitos pela API da carteira; valores em reais como decimal, nunca ponto flutuante. */
final class Requests {

  private Requests() {}

  record ContributionRequest(@NotNull @Positive @Nullable BigDecimal amount) {}

  record OrderRequest(
      @NotBlank @Nullable String ticker,
      @NotNull @Nullable OrderSide side,
      @Positive long quantity) {}

  record FixedIncomeRequest(
      @NotBlank @Nullable String productId, @NotNull @Positive @Nullable BigDecimal amount) {}
}
