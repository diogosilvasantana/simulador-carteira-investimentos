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
3. Variáveis: `POSTGRES_PASSWORD` como segredo (diferente em cada ambiente)
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
