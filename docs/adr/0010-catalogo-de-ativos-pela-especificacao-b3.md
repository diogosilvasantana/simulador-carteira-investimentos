# ADR-010: Catálogo de ativos pela especificação B3 do MT5, enriquecido por fontes oficiais

- **Status:** Aceito
- **Data:** 2026-11-24

## Contexto

A vitrine e as listas de cotações precisam separar os ativos por categoria (ações, FIIs, Fiagros,
ETFs, BDRs), filtrar por setor e segmento e listar todos os ativos da bolsa sem deixar a aplicação
pesada.

A sonda do catálogo (`mt5-bridge/spikes/mt5_catalog_probe.py`) rodada no MT5 da XP mostrou que:

- a pasta é a mesma para todo o mercado à vista (`BOVESPA\A VISTA\<ticker>`) e só separa índices
  (`BOVESPA\INDICES`) e BM&F
- setor, indústria, categoria e ISIN vêm vazios
- a descrição traz, em largura fixa, 12 caracteres de nome e a especificação oficial da B3
  (`PETROBRAS   PN      N2`, `FII MAXI RENCI`, `APPLE       DRN`), com segmento de listagem
  (`NM`, `N1`, `N2`) e marcas de negociação ex (`ED`, `EJ`, `ER`)
- são cerca de 2.470 ativos entre ações, units, FIIs, Fiagros, ETFs e BDRs

Fontes oficiais e gratuitas cobrem o que o MT5 não tem: o Cadastro de Instrumentos da B3
(BVBG.028, diário, com a categoria do papel), a Classificação Setorial da B3 (setor, subsetor e
segmento das empresas) e o Informe Mensal Estruturado de FII da CVM (segmento de atuação dos
fundos).

## Opções consideradas

1. **Só o MT5**: zero dependência externa e funciona no primeiro tick, mas sem setor nem segmento
   de FII, e com o tipo inferido da descrição.
2. **Só as fontes oficiais**: classificação exata, mas depende de download agendado e não conhece
   um ativo listado hoje até o próximo arquivo.
3. **MT5 como base, fontes oficiais como enriquecimento**: o tipo sai da especificação B3 que o
   MT5 já entrega; setor e segmento vêm dos arquivos oficiais quando existirem.

## Decisão

Opção 3, em duas etapas:

1. **Agora:** a ponte envia uma mensagem `asset` com ticker, descrição e pasta de cada ativo ao
   conectar. O domínio de `marketdata` classifica pela especificação B3 (`AssetType`), agrupa em
   categorias (`AssetCategory`) e extrai o segmento de listagem (`ListingSegment`).
2. **Depois:** importadores agendados do Cadastro de Instrumentos (confirma o tipo), da
   Classificação Setorial (setor das ações) e do Informe Mensal de FII (segmento dos fundos).
   Quando a fonte oficial discordar da inferência, ela vence.

Para listar tudo sem pesar:

- o navegador carrega e assina só a categoria aberta (`GET /api/assets?category=`,
  `/topic/quotes/category/{categoria}`)
- o backend agrupa os ticks e publica no máximo um lote por categoria a cada 500 ms, só com os
  ativos que mudaram (dentro do limite do ADR-006)
- a tela desenha só as linhas visíveis (rolagem virtual)
- no modo `all`, a ponte lê o mercado a cada 1 s em vez de 250 ms

## Consequências

- Categorias funcionam no primeiro dia, só com o MT5
- O nome exibido é o curto da B3 (12 caracteres) até a etapa 2 trazer a razão social
- Outra corretora pode formatar a descrição de outro jeito: o classificador é testado com as
  descrições reais da XP e marca como `OTHER` o que não reconhece, sem quebrar a ingestão
- Índices ficam fora do catálogo por ora: pontos de índice não são dinheiro e pedem outro modelo
- O download automático da B3 não tem link fixo simples; se o importador complicar, a etapa 2
  começa com upload manual do arquivo
