# ADR-002: Arquitetura hexagonal dentro de cada módulo

- **Status:** Aceito
- **Data:** 2026-11-22

## Contexto

As regras de negócio (preço médio, custos, proventos) são o núcleo do aprendizado e precisam ser testadas sem banco, sem Spring e sem rede.

## Opções consideradas

1. **Camadas tradicionais** (controller → service → repository): conhecido, mas a regra de negócio tende a vazar para services anêmicos acoplados ao JPA.
2. **Hexagonal (portas e adaptadores)**: domínio puro no centro, infraestrutura plugada por interfaces.

## Decisão

Cada módulo tem `domain` (Java puro), `application` (casos de uso e portas de saída), `adapters/in` (REST, WebSocket, listeners) e `adapters/out` (JPA, Redis, clientes).

## Consequências

- Testes de domínio rápidos, sem contexto Spring
- `ArchitectureTests` (ArchUnit) proíbe `domain` de depender de Spring ou Jakarta Persistence
- Mais classes (entidade JPA separada do modelo, mapeadores); custo aceito pela clareza
