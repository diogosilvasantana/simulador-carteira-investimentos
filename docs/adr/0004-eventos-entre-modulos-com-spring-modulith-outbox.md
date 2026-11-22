# ADR-004: Eventos entre módulos com Spring Modulith (outbox)

- **Status:** Aceito
- **Data:** 2026-11-22

## Contexto

Ao executar uma ordem, a carteira, os proventos e as métricas precisam reagir, sem que `trading` conheça esses módulos e sem perder eventos se um consumidor falhar.

## Opções consideradas

1. **Chamada direta entre serviços**: acoplamento forte e ciclos de dependência.
2. **Broker externo** (Kafka, RabbitMQ): robusto, mas mais um serviço para operar no mesmo servidor.
3. **Eventos de aplicação com Event Publication Registry do Spring Modulith**: evento gravado na mesma transação e reentregue em caso de falha.

## Decisão

Opção 3. Módulos publicam eventos de domínio (`OrderExecuted`, `PositionChanged`, `DividendPaid`) e consomem com `@ApplicationModuleListener`.

## Consequências

- Desacoplamento sem infraestrutura nova
- Garantia de entrega por outbox no Postgres
- Consumidores são assíncronos: telas leem dados eventualmente consistentes por milissegundos
- Se um dia houver mais de uma instância, o Modulith pode externalizar os eventos para um broker
