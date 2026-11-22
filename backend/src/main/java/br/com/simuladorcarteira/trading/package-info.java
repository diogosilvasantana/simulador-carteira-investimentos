/**
 * Módulo trading: Ordens, validação, execução simulada e custos.
 *
 * <p>Estrutura interna (ADR-002): {@code domain}, {@code application}, {@code adapters.in} e
 * {@code adapters.out}. Outros módulos só usam os tipos públicos deste pacote raiz ou eventos.
 */
@org.jspecify.annotations.NullMarked
@org.springframework.modulith.ApplicationModule(displayName = "trading")
package br.com.simuladorcarteira.trading;
