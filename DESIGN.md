---
name: Simulador de Carteira
description: Home broker simulado com cotações reais da B3, em carvão neutro e índigo.
colors:
  bg: "#0d0e12"
  surface-1: "#14151b"
  surface-2: "#1b1c24"
  surface-3: "#23242e"
  line: "#2a2c37"
  line-strong: "#3a3c4a"
  control-border: "#666a80"
  text: "#ececf1"
  text-muted: "#a3a5b5"
  text-subtle: "#868aa0"
  accent: "#9091ff"
  accent-strong: "#5b5cf0"
  accent-soft: "#1f2040"
  accent-line: "#2c2d5a"
  on-accent: "#ffffff"
  gain: "#3dd68c"
  gain-soft: "#0f2a1e"
  loss: "#ff6b7a"
  loss-soft: "#36161c"
  warning: "#f2b84b"
  warning-soft: "#33270f"
  asset-acoes: "#338ced"
  asset-renda-fixa: "#e76624"
  asset-fiagros: "#00a888"
  asset-fiis: "#d28e05"
  asset-bdrs: "#e24d8b"
  asset-etfs: "#9887ef"
typography:
  display:
    fontFamily: "'Space Grotesk', 'Inter Variable', system-ui, sans-serif"
    fontSize: "34px"
    fontWeight: 600
    lineHeight: "40px"
    letterSpacing: "-0.025em"
  metric:
    fontFamily: "'Space Grotesk', 'Inter Variable', system-ui, sans-serif"
    fontSize: "26px"
    fontWeight: 600
    lineHeight: "34px"
    letterSpacing: "-0.01em"
    fontFeature: "'tnum' 1"
  headline:
    fontFamily: "'Inter Variable', Inter, system-ui, 'Segoe UI', sans-serif"
    fontSize: "22px"
    fontWeight: 600
    lineHeight: "30px"
    letterSpacing: "-0.02em"
  title:
    fontFamily: "'Inter Variable', Inter, system-ui, 'Segoe UI', sans-serif"
    fontSize: "17px"
    fontWeight: 600
    lineHeight: "24px"
    letterSpacing: "-0.01em"
  body:
    fontFamily: "'Inter Variable', Inter, system-ui, 'Segoe UI', sans-serif"
    fontSize: "14px"
    fontWeight: 400
    lineHeight: "20px"
    fontFeature: "'cv11' 1"
  body-small:
    fontFamily: "'Inter Variable', Inter, system-ui, 'Segoe UI', sans-serif"
    fontSize: "12px"
    fontWeight: 400
    lineHeight: "17px"
  label:
    fontFamily: "'Inter Variable', Inter, system-ui, 'Segoe UI', sans-serif"
    fontSize: "12px"
    fontWeight: 500
    letterSpacing: "0.02em"
  seal:
    fontFamily: "'Inter Variable', Inter, system-ui, 'Segoe UI', sans-serif"
    fontSize: "11px"
    fontWeight: 700
    letterSpacing: "0.06em"
rounded:
  xs: "4px"
  sm: "6px"
  md: "10px"
  lg: "14px"
  pill: "999px"
spacing:
  "1": "4px"
  "2": "8px"
  "3": "12px"
  "4": "16px"
  "5": "20px"
  "6": "24px"
  "8": "32px"
  "12": "48px"
components:
  button-primary:
    backgroundColor: "{colors.accent-strong}"
    textColor: "{colors.on-accent}"
    rounded: "{rounded.sm}"
    padding: "0 16px"
    height: "40px"
  button-secondary:
    backgroundColor: "transparent"
    textColor: "{colors.text}"
    rounded: "{rounded.sm}"
    padding: "0 16px"
    height: "40px"
  button-secondary-hover:
    backgroundColor: "{colors.surface-2}"
  input-field:
    backgroundColor: "{colors.surface-2}"
    textColor: "{colors.text}"
    rounded: "{rounded.sm}"
    padding: "0 12px"
    height: "40px"
  input-search:
    backgroundColor: "{colors.surface-1}"
    textColor: "{colors.text}"
    rounded: "{rounded.pill}"
    padding: "0 12px"
    height: "36px"
  nav-item:
    backgroundColor: "transparent"
    textColor: "{colors.text-muted}"
    rounded: "{rounded.sm}"
    padding: "0 12px"
    height: "40px"
  nav-item-hover:
    backgroundColor: "{colors.surface-2}"
    textColor: "{colors.text}"
  nav-item-active:
    backgroundColor: "{colors.accent-soft}"
    textColor: "{colors.accent}"
  type-tile:
    backgroundColor: "{colors.surface-1}"
    textColor: "{colors.text}"
    rounded: "{rounded.md}"
    padding: "16px"
    height: "144px"
  type-tile-active:
    backgroundColor: "{colors.accent-soft}"
    textColor: "{colors.text}"
  panel:
    backgroundColor: "{colors.surface-1}"
    textColor: "{colors.text}"
    rounded: "{rounded.md}"
    padding: "16px 20px"
  sim-seal:
    backgroundColor: "{colors.warning-soft}"
    textColor: "{colors.warning}"
    typography: "{typography.seal}"
    rounded: "{rounded.pill}"
    padding: "0 12px"
    height: "24px"
  status-pill:
    backgroundColor: "transparent"
    textColor: "{colors.text-muted}"
    rounded: "{rounded.pill}"
    padding: "0 12px"
    height: "28px"
  change-up:
    backgroundColor: "{colors.gain-soft}"
    textColor: "{colors.gain}"
    rounded: "{rounded.xs}"
    padding: "1px 6px"
  change-down:
    backgroundColor: "{colors.loss-soft}"
    textColor: "{colors.loss}"
    rounded: "{rounded.xs}"
    padding: "1px 6px"
  tag-best:
    backgroundColor: "{colors.accent-strong}"
    textColor: "{colors.on-accent}"
    rounded: "{rounded.pill}"
    padding: "0 8px"
    height: "20px"
  tag-exempt:
    backgroundColor: "{colors.gain-soft}"
    textColor: "{colors.gain}"
    rounded: "{rounded.pill}"
    padding: "0 8px"
    height: "20px"
  tag-warn:
    backgroundColor: "{colors.warning-soft}"
    textColor: "{colors.warning}"
    rounded: "{rounded.pill}"
    padding: "0 8px"
    height: "20px"
  asset-monogram:
    textColor: "{colors.bg}"
    rounded: "{rounded.sm}"
    size: "32px"
---

# Design System: Simulador de Carteira

<!-- Registrado a partir do build (frontend/src/styles.scss, shell em frontend/src/app/app.*, shared/ e features/{home,quotes,fixed-income}). O Design System aprovado "Carvão e Índigo" também vive como artifact em https://claude.ai/artifact/FWFzawgZnJurbJ3fsNu5VT; os tokens aqui usam os mesmos nomes. -->

## Overview

**Creative North Star: "A Mesa de Treino Noturna"**

Uma mesa de operação escura e calma onde o preço é o protagonista. O fundo é carvão neutro em camadas (sem matiz quente nem azul de "fintech"), e quase tudo é texto e número sobre essas camadas, separados por linhas finas. A cor aparece com função: índigo só para marca, seleção e ação; verde-menta e rosa só para alta e baixa, sempre com seta e sinal; as cores de classe de ativo só em traços curtos e monogramas. O selo âmbar SIMULADO fica no topo de toda tela, porque o produto nunca pode parecer uma corretora real.

A densidade é de ferramenta, não de landing page: corpo de 14 px, linhas de tabela de 56 px, números tabulares alinhados à direita. A personalidade vem do Space Grotesk em poucos pontos (marca, título de abertura, números grandes e monogramas) contra um Inter discreto em todo o resto. O movimento é curto e informativo: só o preço que mudou pisca, e tudo respeita a redução de movimento.

**Key Characteristics:**
- Carvão em quatro camadas (`bg`, `surface-1..3`) com profundidade por tom e linha, não por sombra
- Índigo raro e com significado: marca, item ativo, ação primária, seleção, foco
- Alta e baixa nunca só pela cor: seta desenhada em CSS mais sinal `+`/`-`
- Cores de classe de ativo em traços de 24 × 4 px e monogramas de 32 px, nunca em superfícies grandes
- Selo SIMULADO âmbar sempre visível na faixa superior
- Números com `tabular-nums`, alinhados à direita

## Colors

Carvão neutro em camadas, um índigo de ação e três sinais semânticos; as cores de classe de ativo formam uma família à parte, só para identificação.

### Primary
- **Índigo de Ação** (`accent-strong`): fundo do botão primário, da marca, da tag "Melhor líquido" e da seleção de texto. É o único preenchimento cheio colorido da interface.
- **Índigo Claro** (`accent`): texto e borda de estado ativo (item de menu, card do carrossel selecionado), links, triângulo de ordenação, borda de campo em foco e anel de foco.
- **Índigo Profundo** (`accent-soft`, com `accent-line` como divisória): fundo tonal do que está selecionado ou em destaque, como o item de menu ativo, o card ativo do carrossel e a linha vencedora da tabela de renda fixa.

### Secondary
- **Menta de Alta** (`gain`, sobre `gain-soft`): variação positiva, piscada de preço em alta, rendimento líquido, status "conectado", tag "isento de IR" e fase "Disponível".
- **Rosa de Baixa** (`loss`, sobre `loss-soft`): variação negativa, piscada de preço em baixa, erro de campo, aviso de erro e status "desconectado".
- **Âmbar de Simulação** (`warning`, sobre `warning-soft`): o selo SIMULADO, status "conectando", tag de carência e aviso de atenção.

### Tertiary
- **Paleta de Classes de Ativo** (`asset-acoes`, `asset-renda-fixa`, `asset-fiis`, `asset-fiagros`, `asset-etfs`, `asset-bdrs`): identificam a classe no traço do card do carrossel e no monograma da tabela de cotações. A ordem é fixa (a mesma dos gráficos e da alocação do artifact).

### Neutral
- **Carvão Base** (`bg`): fundo da página e da faixa superior; também é a cor do texto dentro dos monogramas.
- **Carvão 1** (`surface-1`): menu lateral, cards, painéis, tabelas, formulário e campo de busca.
- **Carvão 2** (`surface-2`): hover de linhas, itens e botões; fundo de campos de formulário; esqueleto de carregamento.
- **Carvão 3** (`surface-3`): fundo de `code` inline.
- **Linha** (`line`) e **Linha Forte** (`line-strong`): divisórias, bordas de card e de tabela; a forte marca o cabeçalho de tabela, o hover de card e a borda tracejada do estado vazio.
- **Borda de Controle** (`control-border`): borda de botões secundários e campos, mais clara que as linhas para que o controle se leia como interativo.
- **Texto** (`text`), **Texto Apagado** (`text-muted`) e **Texto Sutil** (`text-subtle`): conteúdo principal; descrições, rótulos e cabeçalhos de coluna; notas de rodapé, contagens e placeholders.

### Named Rules
**A Regra do Índigo com Função.** Índigo só marca marca, seleção, ação e foco. Nunca decora um título, um ícone solto ou um fundo de seção.

**A Regra da Seta e do Sinal.** Verde e rosa nunca carregam sozinhos o sentido de alta ou baixa: toda variação leva a seta desenhada em CSS, o sinal e um texto para leitor de tela ("Alta de", "Baixa de").

**A Regra do Traço de Classe.** Cor de classe de ativo aparece em traço curto ou monograma, nunca como fundo de card, de seção ou de linha.

## Typography

**Display Font:** Space Grotesk (com Inter Variable e system-ui de reserva), carregado nos pesos 500 e 600
**Body Font:** Inter Variable (com Inter, system-ui e Segoe UI de reserva), com `cv11` ligado

**Character:** Inter leva a interface inteira com sobriedade de terminal; Space Grotesk entra em poucos pontos para dar voz própria à marca e aos números grandes.

### Hierarchy
- **Display** (600, 34 px / 40 px, −0,025em; 26 px / 32 px no celular): só o título de abertura da página inicial, em Space Grotesk, com largura máxima de 760 px.
- **Metric** (600, 26 px / 34 px, −0,01em, números tabulares): valor grande de indicador, como a taxa anual do CDI; a unidade ao lado volta para Inter 13 px 500 em `text-muted`.
- **Headline** (600, 22 px / 30 px, −0,02em): o H1 de cada tela, uma vez só, em Inter.
- **Title** (600, 17 px / 24 px, −0,01em): o H2 de bloco ("Tipos de ativo", "Compare os produtos"). Rótulos de card como o nome do tipo no carrossel usam 15 px 600.
- **Body** (400, 14 px / 20 px): texto corrente e células de tabela. Textos de apoio (`page-head__lead`) limitados a 68ch.
- **Body Small** (400, 12 px / 17 px): descrição de card, nome do ativo sob o ticker, notas de rodapé (12 px / 18 px, até 80ch).
- **Label** (500, 12 px, 0,02em, maiúsculas): cabeçalho de coluna de tabela; em caixa normal, rótulo de campo de formulário.
- **Seal** (700, 11 px, 0,06em, maiúsculas): só o selo SIMULADO.

### Named Rules
**A Regra do Número Tabular.** Todo número que se compara com outro (preço, variação, volume, horário, valor em reais, taxa) usa `tabular-nums` e se alinha à direita na tabela.

**A Regra do Grotesk Raro.** Space Grotesk só na marca, no título de abertura, em números de destaque e nos monogramas. Títulos de tela e de bloco ficam em Inter.

## Layout

A moldura é uma grade de duas colunas: menu lateral recolhível à esquerda (232 px aberto, 64 px recolhido, só ícones e nomes no `title`) e a área principal, com transição de 140 ms na largura. O menu é fixo na altura da janela, com a marca no topo, os itens no meio e "Recolher" no pé.

A área principal começa por uma faixa superior fixa de 56 px, no fundo `bg` com linha inferior: à esquerda o status da conexão de cotações, só na tela de Cotações (nas outras o espaço fica vazio, porque não há conexão aberta), e à direita o selo SIMULADO. O conteúdo fica num contêiner de até 1200 px centralizado, com respiro de 32 px nas laterais e no topo e 48 px no pé.

O ritmo vertical usa a escala de espaço de base 4 (4, 8, 12, 16, 20, 24, 32, 48). Seções de uma tela se separam por 48 px (início e renda fixa) ou 32 px (cotações, mais densa); dentro de um bloco, título e conteúdo ficam a 8–16 px. Grades de cards usam `auto-fit` com mínimo de 200 px e intervalo de 12 px.

Em Cotações, o carrossel de tipos vem logo abaixo do título e a tabela ao vivo ocupa o resto: só a lista rola (rolagem virtual, linhas de 56 px), o cabeçalho e o carrossel ficam no lugar.

**Celular (até 760 px):** o menu vira uma barra fixa embaixo, com ícone e nome empilhados em alvos de 52 px e respeitando `safe-area-inset-bottom`; marca e "Recolher" somem. Margens laterais caem para 16 px, o conteúdo ganha folga inferior para a barra. O carrossel perde as setas e usa cards de 148 px com rolagem por toque; a tabela de cotações mostra só ativo, último e variação; a tabela de renda fixa mostra só produto, líquido e rendimento.

## Elevation & Depth

O sistema é plano. Profundidade vem de camadas tonais de carvão (`bg` → `surface-1` → `surface-2` → `surface-3`) e de linhas de 1 px, nunca de sombra em repouso. Hover sobe um degrau de carvão; seleção troca para o índigo profundo com borda índigo.

### Shadow Vocabulary
- **Pop** (`box-shadow: 0 12px 32px rgba(0, 0, 0, 0.45)`): reservada para camadas flutuantes. Está definida como token, mas nenhuma tela construída a usa ainda.
- **Anel de foco** (`box-shadow: 0 0 0 2px var(--bg), 0 0 0 4px var(--accent)`): foco visível de todo elemento interativo, com um anel de 2 px do fundo separando o índigo do componente.

### Named Rules
**A Regra do Degrau de Carvão.** Para destacar uma superfície, suba um degrau de carvão ou troque a linha; não adicione sombra.

## Shapes

Cantos suavemente arredondados e consistentes por tamanho: 4 px para peças pequenas dentro do texto (pílula de variação, piscada de preço, `code`, ossos do esqueleto), 6 px para controles e itens (botões, campos, itens de menu, monogramas, marca), 10 px para contêineres (cards, painéis, tabelas, formulário, avisos, estado vazio) e pílula para o que é rótulo de estado (selo SIMULADO, status de conexão, tags, busca, setas do carrossel). O token de 14 px existe mas não aparece nas telas construídas.

Bordas são de 1 px: `line` em contêineres, `control-border` em controles. O estado vazio usa borda tracejada em `line-strong`. Setas de variação e de ordenação são triângulos de 8 × 5 px recortados com `clip-path`.

## Components

### Buttons
Firmes e baixos, sem ornamento.
- **Shape:** cantos de 6 px, altura mínima de 40 px, 16 px nas laterais, texto 600.
- **Primary:** índigo de ação cheio com texto branco; no hover escurece para `#4b4ce0`.
- **Secondary:** transparente com borda `control-border` e texto `text`; no hover ganha fundo `surface-2`.
- **Hover / Focus:** transição de 140 ms em fundo e borda; foco pelo anel índigo. Desabilitado cai para 45% de opacidade.

### Chips e tags
- **Variação:** pílula de 4 px com fundo tonal (`gain-soft`/`loss-soft`), texto 13 px 600 na cor do sinal, seta à esquerda; sem variação fica em `text-muted` sem fundo.
- **Tags de produto:** pílulas de 20 px, 11 px 600. "Melhor líquido" em índigo cheio; "isento de IR" em menta tonal; carência em âmbar tonal.
- **Status de conexão:** pílula de 28 px com borda `line`, ponto de 8 px colorido (menta conectado, âmbar conectando, rosa desconectado, que também tinge texto e borda).
- **Selo SIMULADO:** pílula âmbar tonal de 24 px, 11 px 700 em maiúsculas; o `title` explica que nenhuma ordem chega à B3.

### Cards / Containers
- **Corner Style:** 10 px.
- **Background:** `surface-1`, sobre o `bg` da página.
- **Shadow Strategy:** nenhuma (ver Elevation & Depth).
- **Border:** 1 px `line`.
- **Internal Padding:** 16 px (cards do carrossel), 16 × 20 px (indicadores), 20 px (formulário).
- **Avisos:** mesma forma, 12 × 16 px; neutro com borda `line-strong`, de atenção em âmbar tonal, de erro em rosa tonal, ambos sem borda.

### Inputs / Fields
- **Style:** fundo `surface-2`, borda 1 px `control-border`, cantos de 6 px, 40 px de altura, números tabulares. O rótulo fica acima, 12 px 500 em `text-muted`, a 6 px do campo.
- **Focus:** borda passa para índigo claro, mais o anel de foco.
- **Error:** borda rosa com `aria-invalid`, e uma dica de 13 px em rosa logo abaixo do formulário.
- **Busca:** pílula de 36 px em `surface-1` com ícone de lupa à esquerda; o foco tinge a borda de índigo.

### Navigation
- **Menu lateral:** fundo `surface-1` com linha à direita. Itens de 40 px com ícone de traço de 20 px e nome em 500 `text-muted`; hover sobe para `surface-2` e `text`; ativo em índigo profundo com texto índigo claro e `aria-current="page"`. Recolhido, mostra só os ícones centralizados.
- **Barra inferior (celular):** a mesma lista em linha, cada item com ícone sobre nome de 11 px, o ativo com o mesmo fundo índigo profundo.
- **Ícones:** desenhados para o produto em 24 × 24, traço de 1,75, pontas arredondadas, sempre `currentColor`.
- **Marca:** quadrado índigo de 32 px, cantos de 6 px, com a linha de um gráfico subindo em branco; ao lado, "Simulador de Carteira" em Space Grotesk 15 px 600. A marca é provisória.

### Carrossel de tipos de ativo
Escolha explícita do dono do produto, usado na página inicial e em Cotações. Uma faixa horizontal com rolagem e encaixe por card (mínimo de 196 px no computador, 148 px no celular), com setas redondas de 32 px acima, à direita, só no computador. Cada card tem 144 px de altura mínima: traço da cor da classe (24 × 4 px), nome em 15 px 600, descrição em 12 px apagado e rodapé com a contagem de ativos em `text-subtle`. Hover sobe um degrau de carvão e a borda para `line-strong`; o card ativo ganha fundo índigo profundo, borda índigo claro e `aria-current`.

### Tabela de cotações ao vivo
O componente central. Contêiner `surface-1` de 10 px, cabeçalho de 40 px em rótulo maiúsculo com linha forte, linhas de 56 px separadas por `line`, hover em `surface-2`. A primeira coluna junta o monograma (32 px, cantos de 6 px, duas primeiras letras do ticker em Space Grotesk 12 px 600 sobre a cor da classe, texto em `bg`), o ticker em 600 e o nome em 12 px apagado. Colunas ordenáveis mostram um triângulo índigo no sentido da ordem e expõem `aria-sort`. Enquanto carrega, ossos no formato das células pulsam.

**Piscada de preço:** quando o último preço muda, só ele ganha fundo tonal e texto na cor do sentido (menta ou rosa), que somem em 600 ms com `cubic-bezier(0.16, 1, 0.3, 1)`. O resto da linha não se mexe.

### Tabela de comparação de renda fixa
Mesma moldura da tabela de cotações, como `<table>` semântica, com rolagem horizontal própria. A linha do melhor líquido recebe fundo índigo profundo e divisória `accent-line`; linhas bloqueadas por carência ficam em `text-subtle`. O tipo do produto aparece em 11 px 600 maiúsculo antes do nome, e o rendimento líquido em menta com sinal `+`.

### Componentes ainda por construir
Gráficos, boleta de ordem, mapa de calor, carteira e alocação existem no Design System aprovado (artifact em https://claude.ai/artifact/FWFzawgZnJurbJ3fsNu5VT) e ainda não estão no Angular. Use o artifact como referência quando forem construídos e registre aqui o que for entregue.

## Do's and Don'ts

### Do:
- **Do** usar só os tokens de `styles.scss` (com os mesmos nomes do artifact) para cor, espaço, raio e movimento.
- **Do** manter o selo SIMULADO na faixa superior de toda tela.
- **Do** acompanhar toda alta e baixa com seta, sinal e texto para leitor de tela.
- **Do** usar `tabular-nums` e alinhar à direita todo número comparável.
- **Do** destacar seleção com índigo profundo (`accent-soft`) e borda ou texto índigo claro (`accent`).
- **Do** mostrar o status da conexão só onde há fluxo de cotações aberto.
- **Do** desenhar o esqueleto de carregamento no formato do conteúdo que vai chegar.
- **Do** deixar o movimento curto (140 ms em estados, 600 ms na piscada) e respeitar `prefers-reduced-motion`.

### Don't:
- **Don't** usar verde ou rosa como única pista de alta e baixa.
- **Don't** pintar cards, seções ou linhas inteiras com a cor de classe de ativo; ela fica no traço e no monograma.
- **Don't** usar índigo como decoração de título, ícone ou fundo de seção.
- **Don't** adicionar sombra a superfícies em repouso; a profundidade é tonal.
- **Don't** repetir o título da tela na faixa superior; o H1 aparece uma vez só, na página.
- **Don't** mostrar dados que pareçam reais em áreas ainda sem backend (carteira, ordens, proventos).
- **Don't** copiar cores ou estrutura do Íon (Itaú); a referência é só estrutural e inspiracional.
