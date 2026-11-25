package br.com.simuladorcarteira.fixedincome.adapters.out.config;

import br.com.simuladorcarteira.fixedincome.domain.IncomeTaxTable;
import br.com.simuladorcarteira.fixedincome.domain.IofTable;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Tabelas de IR e IOF da configuração, cada uma com a data em que passou a valer (ADR-011). */
@ConfigurationProperties("simulador.fixedincome.taxes")
record TaxProperties(List<IncomeTaxTable> incomeTax, List<IofTable> iof) {}
