package br.com.simuladorcarteira.marketdata.adapters.out.stomp;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuração do WebSocket do navegador.
 *
 * @param allowedOrigins origens aceitas no endpoint {@code /ws}
 */
@ConfigurationProperties("simulador.realtime")
record RealtimeProperties(@DefaultValue("http://localhost:4200") List<String> allowedOrigins) {}
