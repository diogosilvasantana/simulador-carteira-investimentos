# ADR-006: STOMP sobre WebSocket para tempo real

- **Status:** Aceito
- **Data:** 2026-11-22

## Contexto

O navegador precisa receber cotações e P&L em tempo real, assinando apenas os ativos e carteiras visíveis.

## Opções consideradas

1. **Server-Sent Events**: simples, mas unidirecional e sem roteamento por tópico nativo.
2. **WebSocket puro**: flexível, mas exige protocolo próprio de assinatura.
3. **STOMP sobre WebSocket**: tópicos (`/topic/quotes/{ticker}`), suporte nativo no Spring e cliente maduro para Angular.

## Decisão

Opção 3, com broker simples em memória no MVP e limite de 4 atualizações por segundo por ativo.

## Consequências

- Assinatura por demanda reduz tráfego
- Com mais de uma instância, troca-se o broker em memória por relay (Redis ou RabbitMQ)
