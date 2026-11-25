package br.com.simuladorcarteira.fixedincome.adapters.out.bcb;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Endereço da API de séries temporais do Banco Central (SGS). */
@ConfigurationProperties("simulador.fixedincome.bcb")
record BcbProperties(@DefaultValue("https://api.bcb.gov.br") String baseUrl) {}
