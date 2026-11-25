package br.com.simuladorcarteira.portfolio.domain;

import br.com.simuladorcarteira.shared.Money;
import br.com.simuladorcarteira.shared.Ticker;

/** Prévia de uma ordem: o que a etapa de revisão mostra antes de confirmar. */
public record OrderQuote(
    OrderSide side,
    Ticker ticker,
    long quantity,
    Money price,
    Money gross,
    Money fees,
    Money total) {}
