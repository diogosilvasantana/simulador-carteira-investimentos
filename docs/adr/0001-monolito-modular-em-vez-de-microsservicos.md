# ADR-001: Monólito modular em vez de microsserviços

- **Status:** Aceito
- **Data:** 2026-11-22

## Contexto

Um desenvolvedor, em tempo parcial, um único servidor Coolify (2 vCPU, 8 GB) e um domínio com regras financeiras que precisam de transações fortes (ordem → execução → posição → caixa).

## Opções consideradas

1. **Microsserviços** (um serviço por contexto): escala e deploy independentes, mas trazem rede, consistência eventual, observabilidade distribuída e custo operacional sem ganho para este tamanho.
2. **Monólito tradicional em camadas**: simples, mas as fronteiras de negócio se perdem com o tempo.
3. **Monólito modular**: um deploy, módulos de negócio com fronteiras verificadas em teste.

## Decisão

Monólito modular com Spring Modulith. Cada bounded context (`identity`, `marketdata`, `trading`, `portfolio`, `corporateactions`, `analytics`) é um pacote de primeiro nível com API pública explícita.

## Consequências

- Transações locais e simples para o fluxo financeiro
- `ModularityTests` quebra o build se um módulo acessar o interior de outro
- Um módulo pode ser extraído para serviço próprio no futuro, porque já conversa por eventos
- Escala horizontal fica limitada até lá, aceitável para menos de 10 usuários simultâneos
