/**
 * Módulo fixedincome: indexadores (CDI, Selic, IPCA) e renda fixa simulada (ADR-011).
 *
 * <p>Estrutura interna (ADR-002): {@code domain}, {@code application}, {@code adapters.in} e
 * {@code adapters.out}. Outros módulos só usam os tipos públicos deste pacote raiz ou eventos.
 */
@org.jspecify.annotations.NullMarked
@org.springframework.modulith.ApplicationModule(displayName = "fixedincome")
package br.com.simuladorcarteira.fixedincome;
