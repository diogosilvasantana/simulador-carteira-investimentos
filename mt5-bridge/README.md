# mt5-bridge

Ponte em Python que lê cotações do terminal MetaTrader 5 e envia ao backend pela rede Tailscale
(ADR-003). Roda no Windows, ao lado do MT5. Só lê dados: não envia ordens.

## Estado

- `spikes/mt5_spike.py`: validação inicial (conexão, ativos disponíveis, frequência e atraso)
- `src/mt5_bridge/protocol.py`: formato das mensagens de ingestão, compartilhado com o simulador
- `src/mt5_bridge/config.py`: configuração por variáveis de ambiente
- Loop de envio: issue da fase 2

## Rodar o spike (Windows, MT5 aberto e logado)

```powershell
pip install MetaTrader5
python spikes/mt5_spike.py PETR4 VALE3 MXRF11 --seconds 120
```

## Desenvolvimento (qualquer sistema)

```bash
pip install -e ".[dev]"
ruff check . && ruff format --check . && mypy . && pytest
```

## Configuração

| Variável | Exemplo |
| --- | --- |
| `BRIDGE_INGEST_URL` | `wss://simulador.tail1234.ts.net/ingest` |
| `BRIDGE_INGEST_TOKEN` | token gerado no backend |
| `BRIDGE_SYMBOLS` | `PETR4,VALE3,MXRF11` |
| `BRIDGE_POLL_INTERVAL_MS` | `250` |

## Rodar a ponte

```powershell
$env:BRIDGE_INGEST_TOKEN = "local-dev-token"
python -m mt5_bridge --source mt5 --symbols PETR4,VALE3      # MT5 aberto e logado
python -m mt5_bridge --source synthetic --symbols PETR4 --seed 7   # mercado fechado
```

Passo a passo completo no Windows: [docs/guias/rodar-local-windows.md](../docs/guias/rodar-local-windows.md).
