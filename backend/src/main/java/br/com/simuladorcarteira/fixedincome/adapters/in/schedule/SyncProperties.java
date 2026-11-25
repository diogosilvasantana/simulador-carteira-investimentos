package br.com.simuladorcarteira.fixedincome.adapters.in.schedule;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Quanto histórico buscar na primeira carga dos indexadores. */
@ConfigurationProperties("simulador.fixedincome.sync")
record SyncProperties(@DefaultValue("3") int backfillYears) {}
