# ADR-007: Monorepo

- **Status:** Aceito
- **Data:** 2026-11-22

## Contexto

Backend, frontend, ponte e infraestrutura evoluem juntos; mudanças de API afetam o cliente Angular gerado do OpenAPI. Projeto de um desenvolvedor, com objetivo de portfólio.

## Opções consideradas

1. **Repositórios separados** por aplicação: ciclos de release independentes, mas mudanças coordenadas em vários PRs.
2. **Monorepo** com builds independentes por pasta.

## Decisão

Monorepo (aceito em 22/11/2026). Cada aplicação tem seu build; a CI usa filtros por caminho para rodar só o que mudou.

## Consequências

- Mudanças atômicas entre API e frontend no mesmo PR
- Quem visita o repositório vê o sistema inteiro e as decisões num lugar só
- Histórico misturado, mitigado por Conventional Commits com escopo
- Uma pasta pode ser extraída para outro repositório, mantendo o histórico, se virar produto à parte
