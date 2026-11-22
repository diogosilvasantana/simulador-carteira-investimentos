# Como contribuir

## Fluxo

1. Escolha uma issue do quadro (milestone da fase atual)
2. Crie uma branch curta a partir da `main`: `feat/123-preco-medio`, `fix/…`, `chore/…`, `docs/…`
3. Escreva os testes primeiro, depois o código
4. Rode as verificações locais (abaixo)
5. Abra o pull request preenchendo o checklist do template
6. A CI precisa ficar verde antes do merge; a `main` é protegida

## Commits

Usamos [Conventional Commits](https://www.conventionalcommits.org/pt-br/) com escopo:

```
feat(trading): executar ordem a mercado ao último preço
fix(portfolio): corrigir arredondamento do preço médio
docs(adr): ADR-008 cache de cotações
chore(ci): filtrar jobs por caminho
```

Tipos: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `ci`, `build`, `perf`.
Escopos: `backend`, `frontend`, `bridge`, `infra`, `docs` ou o nome de um módulo
(`trading`, `portfolio`, `marketdata`…).

## Verificações locais

```bash
cd backend && mvn verify                       # testes, arquitetura, formatação, cobertura
cd frontend && npm run lint && npm test -- --watch=false && npm run build
cd mt5-bridge && ruff check . && ruff format --check . && mypy .
```

## Definition of Done

- Testes novos cobrindo o comportamento, todos verdes na CI
- Quality gate aprovado, sem novo code smell crítico
- Nomes seguem o glossário; nenhuma regra de negócio fora do `domain`
- API documentada no OpenAPI e cliente Angular regenerado, se mudou
- Migration Flyway incluída, se mudou o banco
- ADR escrito, se houve decisão de arquitetura
- README ou docs atualizados, se mudou algo que quem chega precisa saber
