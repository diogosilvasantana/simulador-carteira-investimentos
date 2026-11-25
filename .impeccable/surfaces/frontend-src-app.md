---
version: 1
slug: "frontend-src-app"
primary_target: "frontend/src/app"
related_targets: []
---

# App shell e telas atuais do frontend

Modo: Operate. Público: autor, iniciantes e investidores testando estratégias. Tarefa: acompanhar cotações ao vivo, comparar renda fixa e navegar entre áreas do simulador.

Escopo: moldura do app (menu lateral recolhível, cabeçalho, barra inferior no celular) e as telas existentes: Início, Cotações e Renda fixa. Não inventar telas sem backend.

## Direction contract

THESIS: uma mesa de treino escura e calma onde o preço é o protagonista; recusa o painel genérico de cards iguais e a cópia da estrutura do Íon.
OWN-WORLD: Carvão e Índigo do Design System aprovado: carvão neutro em camadas, índigo só para marca, seleção e ação, verde-menta e rosa para alta e baixa com seta, cores de classe de ativo em traços e monogramas.
STORY: o usuário entende de relance se o mercado está ao vivo, escolhe o tipo de ativo pelo carrossel, acompanha preços que piscam ao mudar e compara renda fixa pelo líquido.
FIRST VIEWPORT: menu lateral recolhível à esquerda (232 px aberto, 64 px fechado; barra inferior no celular), faixa fina no topo de toda tela com o selo SIMULADO à direita e, só em Cotações, o status da conexão à esquerda (o fluxo de cotações só abre nessa tela; nas outras não há conexão cujo estado mostrar); o título da tela fica no H1 da página, uma vez só; em Cotações, o carrossel de tipos logo abaixo do título e a tabela ao vivo ocupando o resto.
FORM: established world (Design System aprovado); seed: none (pinned) "Eu gostei mais do menu lateral como estava" e "vc tirou meu carrossel de tipos que gostei".
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
