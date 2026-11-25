/**
 * Módulo identity: Login do dono da carteira, com sessão no servidor (ADR-014).
 *
 * <p>Estrutura interna (ADR-002): {@code domain}, {@code application}, {@code adapters.in} e
 * {@code adapters.out}. Outros módulos só usam os tipos públicos deste pacote raiz ou eventos.
 */
@org.jspecify.annotations.NullMarked
@org.springframework.modulith.ApplicationModule(displayName = "identity")
package br.com.simuladorcarteira.identity;
