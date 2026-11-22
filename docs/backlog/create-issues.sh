#!/usr/bin/env bash
# Cria o milestone, os rótulos e as issues da fase 1 a partir de fase-1.json.
# Uso: bash docs/backlog/create-issues.sh   (requer gh autenticado e jq)
set -euo pipefail
cd "$(dirname "$0")"

MILESTONE="Fase 1 · Núcleo"
gh api repos/{owner}/{repo}/milestones -f title="$MILESTONE" \
  -f description="Carteiras, ordens a mercado, posição e preço médio" >/dev/null 2>&1 || true

for label in história chore docs ci bug backend frontend bridge infra identity marketdata trading portfolio corporateactions analytics; do
  gh label create "$label" --force >/dev/null
done

jq -c '.[]' fase-1.json | while read -r issue; do
  title=$(jq -r '.title' <<<"$issue")
  body=$(jq -r '.body' <<<"$issue")
  labels=$(jq -r '.labels | join(",")' <<<"$issue")
  gh issue create --title "$title" --body "$body" --label "$labels" --milestone "$MILESTONE"
done
