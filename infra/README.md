# Infraestrutura

## Local

```bash
cp infra/.env.example infra/.env   # edite a senha
docker compose -f infra/compose.yaml up --build
```

Frontend em http://localhost:8081, com `/api` e `/ws` encaminhados ao backend.

## Ambientes

| Ambiente | Branch | Domínio sugerido | Banco |
| --- | --- | --- | --- |
| Staging | `develop` | `staging.<seu-dominio>` | próprio, dados de teste |
| Produção | `main` | `<seu-dominio>` | próprio, com backup diário |

## Coolify (um recurso por ambiente)

1. Novo recurso → Docker Compose a partir deste repositório, arquivo `infra/compose.yaml`
2. Branch `develop` para staging e `main` para produção
3. Variáveis do recurso (as marcadas como segredo nunca vão para o repositório):

   | Variável | Valor |
   | --- | --- |
   | `POSTGRES_PASSWORD` | senha forte do banco, segredo, diferente em cada ambiente |
   | `APP_USERNAME` | seu usuário de login |
   | `APP_PASSWORD_HASH` | segredo; gere no seu PC com `python scripts/gerar-hash-senha.py` |
   | `ALLOWED_ORIGINS` | `https://financas.zinklab.com.br` (sem ela o tempo real não conecta) |
   | `INGEST_TOKEN` | segredo longo para a ponte do MT5 (fase 2) |

   A senha não passa por ninguém: o script pede a senha no terminal e imprime só o hash.
4. Domínio público só no serviço `frontend` (porta 80); Postgres, Redis e backend ficam internos
5. **Desative** o deploy automático do Coolify: quem dispara é o workflow `Deploy` do GitHub
6. Backup diário do volume `postgres-data` em produção

## Deploy pelo GitHub

O workflow `Deploy` chama a API do Coolify a cada push na `develop` (staging) e na `main`
(produção), e registra o deploy na aba **Environments** do GitHub. Configure em
Settings → Environments, para `staging` e `production`:

| Tipo | Nome | Valor |
| --- | --- | --- |
| Variável | `COOLIFY_URL` | URL do painel, ex.: `https://coolify.seu-dominio` |
| Variável | `COOLIFY_RESOURCE_UUID` | UUID do recurso daquele ambiente |
| Variável | `APP_URL` | URL pública do ambiente |
| Segredo | `COOLIFY_TOKEN` | token de API do Coolify com permissão de deploy |

Em `production`, ative **Required reviewers** para exigir aprovação antes do deploy.

## Rede privada (fase 2)

Instale a Tailscale no host do Coolify e no PC Windows, na mesma tailnet. A porta de ingestão do
backend será publicada só no IP da Tailscale do host (`TAILSCALE_IP`).
