package br.com.simuladorcarteira.fixedincome;

import br.com.simuladorcarteira.shared.Money;

/**
 * Quanto uma aplicação vale hoje: bruto, impostos de um resgate hoje e líquido. Sem indexadores
 * carregados, {@code ratesAvailable} é falso e o valor é o próprio principal.
 */
public record FixedIncomeValue(
    Money gross, Money iof, Money incomeTax, Money net, boolean ratesAvailable) {}
