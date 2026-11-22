# Backlog · Fase 1 (Núcleo)

Milestone: **Fase 1 · Núcleo**. Critério de pronto da fase: preço médio e custos corretos em teste automatizado, rodando com cotações de replay.

Para criar as issues no GitHub: `bash docs/backlog/create-issues.sh` (precisa do `gh` autenticado).

## 1. chore(backend): Maven Wrapper, Error Prone e NullAway

Adicionar `mvnw` ao repositório e ligar Error Prone + NullAway no compilador, conforme a seção de padrões da arquitetura.

**Critérios de aceite**

- [ ] `./mvnw verify` funciona sem Maven instalado
- [ ] Um `null` retornado em API pública marcada com `@NullMarked` quebra a compilação
- [ ] CI e Dockerfile usam o wrapper

Rótulos: `type: chore`, `area: backend`

## 2. chore(ci): SonarQube Cloud com quality gate e selos no README

Conectar o repositório ao SonarQube Cloud (gratuito para repositório público) e enviar a cobertura do JaCoCo.

**Critérios de aceite**

- [ ] Análise roda em todo PR
- [ ] Quality gate reprovado bloqueia o merge
- [ ] Selos de quality gate e cobertura no README

Rótulos: `type: chore`, `type: ci`

## 3. feat(backend): persistência com PostgreSQL, Flyway e Testcontainers

Base de persistência para os módulos: Spring Data JPA, migrations Flyway e testes de integração com Postgres real.

**Critérios de aceite**

- [ ] Migration `V1__baseline.sql` aplicada na subida
- [ ] Teste de integração sobe Postgres via Testcontainers
- [ ] Event Publication Registry do Modulith persistido em JDBC (ADR-004)

Rótulos: `type: feature`, `area: backend`

## 4. feat(identity): cadastro e login com JWT (RF01)

Usuário se cadastra com e-mail e senha e recebe um JWT para acessar a API.

**Critérios de aceite**

- [ ] Senha guardada com BCrypt
- [ ] Login devolve access token de vida curta e refresh token
- [ ] Rotas `/api/**` exigem token, exceto cadastro e login
- [ ] E-mail duplicado devolve 409 em Problem Details

Rótulos: `type: feature`, `module: identity`

## 5. feat(backend): OpenAPI e erros em Problem Details

Documentar a API com springdoc e padronizar erros no formato RFC 9457.

**Critérios de aceite**

- [ ] Swagger UI em `/swagger-ui.html` e contrato em `/v3/api-docs`
- [ ] Exceções de domínio viram Problem Details num único `@RestControllerAdvice`
- [ ] Erro de validação lista os campos inválidos

Rótulos: `type: feature`, `area: backend`

## 6. feat(marketdata): catálogo de ativos a partir do COTAHIST

Importar a série histórica da B3 para montar o catálogo de ações, FIIs, ETFs e BDRs do mercado à vista e seus candles diários.

**Critérios de aceite**

- [ ] Parser do layout COTAHIST com testes sobre um arquivo de amostra
- [ ] Filtra mercado à vista (lote padrão e fracionário)
- [ ] Job diário idempotente: reimportar o mesmo dia não duplica candles
- [ ] Endpoint de busca de ativos por ticker ou nome

Rótulos: `type: feature`, `module: marketdata`

## 7. feat(marketdata): porta MarketDataSource e replay de candles

Fonte de cotações plugável. Primeira implementação: replay dos candles gravados, com velocidade configurável e relógio injetável.

**Critérios de aceite**

- [ ] `QuoteUpdated` publicado a cada novo preço
- [ ] Replay a 1×, 10× e 60× controlado por configuração
- [ ] Testes usam `Clock` fixo, sem `sleep`
- [ ] Última cotação consultável por ticker

Rótulos: `type: feature`, `module: marketdata`

## 8. feat(portfolio): criar carteira com perfil, capital e alocação-alvo (RF02)

Usuário cria carteiras fictícias para praticar.

**Critérios de aceite**

- [ ] Perfil: renda passiva, valorização ou mista
- [ ] Capital inicial vira caixa (`Money`)
- [ ] Alocação-alvo soma 100% ou é recusada
- [ ] Usuário só vê as próprias carteiras (QA7)

Rótulos: `type: feature`, `module: portfolio`

## 9. feat(trading): tabela de custos por execução (RF04)

Corretagem configurável e taxas da B3 calculadas por execução, a partir de tabela versionada por data.

**Critérios de aceite**

- [ ] Valores conferidos na fonte oficial e registrados com data de vigência
- [ ] Nenhuma alíquota fixa no código
- [ ] Testes parametrizados com casos de fronteira de arredondamento

Rótulos: `type: feature`, `module: trading`

## 10. feat(trading): ordem a mercado com idempotência (RF03, QA2)

Enviar ordem de compra ou venda executada ao último preço disponível.

**Critérios de aceite**

- [ ] Chave de idempotência: a mesma ordem enviada duas vezes gera uma única execução
- [ ] Recusa com motivo: sem caixa, sem posição, sem cotação ou feed `STALE`
- [ ] Estados da ordem como `sealed interface`
- [ ] Publica `OrderExecuted` ou `OrderRejected`

Rótulos: `type: feature`, `module: trading`

## 11. feat(portfolio): aplicar execução na posição e no caixa (RF05, QA1)

Ao receber `OrderExecuted`, atualizar posição, preço médio, resultado realizado e caixa.

**Critérios de aceite**

- [ ] Teste do cenário QA1: 3 compras de PETR4 com preços e custos diferentes batem até o centavo
- [ ] Venda não altera o preço médio e registra resultado realizado
- [ ] Venda acima da posição nunca chega aqui (recusada antes)
- [ ] Publica `PositionChanged` e `CashMoved`

Rótulos: `type: feature`, `module: portfolio`

## 12. feat(portfolio): painel da carteira com P&L não realizado

Consultar patrimônio, caixa e posições com resultado não realizado pela última cotação.

**Critérios de aceite**

- [ ] Patrimônio = caixa + soma das posições a mercado
- [ ] Resultado não realizado por ativo e total
- [ ] Mostra a fonte de dados da carteira (LIVE, REPLAY, SYNTHETIC)

Rótulos: `type: feature`, `module: portfolio`

## 13. chore(infra): primeiro deploy no Coolify

Publicar o compose no servidor Coolify com deploy automático na `main`.

**Critérios de aceite**

- [ ] Frontend acessível por domínio com HTTPS
- [ ] Backend, Postgres e Redis sem porta pública
- [ ] Health check do backend controla o deploy
- [ ] Backup diário do Postgres configurado

Rótulos: `type: chore`, `area: infra`

## 14. docs(adr): registrar o resultado do spike MT5 no ADR-003

Rodar `mt5-bridge/spikes/mt5_spike.py` no pregão e decidir se o ADR-003 passa a Aceito.

**Critérios de aceite**

- [ ] Saída do spike anexada (ativos disponíveis, ticks por minuto, atraso p50/p95)
- [ ] ADR-003 atualizado para Aceito ou substituído

Rótulos: `type: docs`, `area: bridge`
