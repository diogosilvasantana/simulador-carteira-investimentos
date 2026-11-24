package br.com.simuladorcarteira.marketdata.adapters.in.ingest;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuração da entrada de ticks.
 *
 * @param token segredo que a ponte envia no cabeçalho {@code Authorization: Bearer}; vazio recusa
 *     todas as conexões, para nunca subir sem proteção por engano
 */
@ConfigurationProperties("simulador.ingest")
public record IngestProperties(@DefaultValue("") String token) {}
