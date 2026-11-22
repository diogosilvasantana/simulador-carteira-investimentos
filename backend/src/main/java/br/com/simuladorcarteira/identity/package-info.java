/**
 * Módulo identity: Usuários, autenticação e emissão de JWT.
 *
 * <p>Estrutura interna (ADR-002): {@code domain}, {@code application}, {@code adapters.in} e
 * {@code adapters.out}. Outros módulos só usam os tipos públicos deste pacote raiz ou eventos.
 */
@org.springframework.modulith.ApplicationModule(displayName = "identity")
package br.com.simuladorcarteira.identity;
