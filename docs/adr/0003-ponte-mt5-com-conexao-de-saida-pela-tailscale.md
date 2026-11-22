# ADR-003: Ponte MT5 com conexão de saída pela Tailscale

- **Status:** Proposto (aguarda spike)
- **Data:** 2026-11-22

## Contexto

A única fonte de tempo real viável e gratuita é o terminal MetaTrader 5 da corretora, que só roda em Windows. O PC do usuário fica em rede doméstica, sem IP fixo.

## Opções consideradas

1. **Backend chama a ponte** (servidor HTTP no PC): exige abrir porta no roteador e expor a máquina pessoal.
2. **MT5 em Wine no servidor**: testado e instável (erro de anti-debugger com versões novas do Wine, quebra do mt5linux em Python 3.11).
3. **Ponte abre conexão de saída** para o backend por WebSocket, dentro de uma rede privada Tailscale, com token próprio.

## Decisão

Opção 3. A ponte Python roda no Windows ao lado do MT5, lê ticks a cada 250 ms e envia ao endpoint `/ingest` do backend, publicado apenas no IP da Tailscale do host.

## Consequências

- Nenhuma porta aberta na rede do usuário; ingestão inacessível pela internet
- Depende do PC ligado durante o pregão (premissa); plano B é mover a ponte para um VPS Windows sem mudar o protocolo
- O simulador de pregão fala o mesmo protocolo, então o sistema funciona sem MT5
- **Pendente de validação:** spike `mt5-bridge/spikes/mt5_spike.py` no pregão
