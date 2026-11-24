/**
 * Tipos de valor compartilhados por todos os módulos, como {@link
 * br.com.simuladorcarteira.shared.Money} e {@link br.com.simuladorcarteira.shared.Ticker}.
 *
 * <p>Só entra aqui o que é estável e não depende de infraestrutura.
 */
@org.jspecify.annotations.NullMarked
@org.springframework.modulith.ApplicationModule(
    type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package br.com.simuladorcarteira.shared;
