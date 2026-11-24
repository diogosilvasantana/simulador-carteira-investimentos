package br.com.simuladorcarteira.marketdata.adapters.in.ingest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/** Mensagem do protocolo de ingestão, igual à de {@code mt5-bridge/src/mt5_bridge/protocol.py}. */
@JsonIgnoreProperties(ignoreUnknown = true)
record IngestMessage(
    @Nullable String type,
    @Nullable String ticker,
    @Nullable String last,
    @Nullable String bid,
    @Nullable String ask,
    @JsonProperty("previous_close") @Nullable String previousClose,
    long volume,
    @JsonProperty("occurred_at") @Nullable String occurredAt,
    @Nullable String source) {}
