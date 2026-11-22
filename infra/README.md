# Infraestrutura

## Local

```bash
cp infra/.env.example infra/.env   # edite a senha
docker compose -f infra/compose.yaml up --build
```

Frontend em http://localhost:8081, com `/api` e `/ws` encaminhados ao backend.

## Coolify

1. Novo recurso → Docker Compose a partir deste repositório, arquivo `infra/compose.yaml`
2. Variáveis: `POSTGRES_PASSWORD` como segredo
3. Domínio público só no serviço `frontend` (porta 80); Postgres, Redis e backend ficam internos
4. Ative o deploy automático por webhook do GitHub na branch `main`
5. Backup diário do volume `postgres-data`

## Rede privada (fase 2)

Instale a Tailscale no host do Coolify e no PC Windows, na mesma tailnet. A porta de ingestão do
backend será publicada só no IP da Tailscale do host (`TAILSCALE_IP`).
