/**
 * Módulo marketdata: Ingestão de ticks, última cotação, candles, replay e catálogo de ativos.
 *
 * <p>Estrutura interna (ADR-002): {@code domain}, {@code application}, {@code adapters.in} e
 * {@code adapters.out}. Outros módulos só usam os tipos públicos deste pacote raiz ou eventos.
 */
@org.springframework.modulith.ApplicationModule(displayName = "marketdata")
package br.com.simuladorcarteira.marketdata;
