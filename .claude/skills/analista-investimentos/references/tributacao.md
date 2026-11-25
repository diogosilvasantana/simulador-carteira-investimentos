# Tributação e garantias (pessoa física residente)

Conferido em 25/11/2026. **Reconfira na fonte oficial antes de cada uso.** "Oficial" = li o texto;
"secundária" = só guias ou escritórios, portanto confirme antes de codificar.

| Item | Regra | Confiança |
| --- | --- | --- |
| Renda fixa tributada | 22,5% até 180 dias; 20% até 360; 17,5% até 720; 15% acima | Configuração do projeto (Lei 11.033/2004) |
| IOF | Regressivo, resgate antes de 30 dias | Configuração do projeto (Decreto 6.306/2007) |
| LCI e LCA | Rendimento isento de IR | ADR-011 |
| LCI (demais casos) e LCA sem índice de preços | Prazo mínimo de **6 meses** | Oficial: [CMN 5.215/2025](https://www.bcb.gov.br/estabilidadefinanceira/exibenormativo?tipo=Resolu%C3%A7%C3%A3o%20CMN&numero=5215) |
| Ações, operação comum | 15% do ganho; isento se vendas do mês até R$ 20 mil | Secundária |
| ETF de ações e BDR | 15%, sem a isenção de R$ 20 mil | Secundária |
| FII | Ganho na venda 20%; rendimentos isentos sob condições | Secundária |
| JCP | 17,5% na fonte desde 01/01/2026 (LC 224/2025) | Secundária, convergente |
| Dividendos | 10% na fonte se a mesma empresa pagar mais de R$ 50 mil no mês (Lei 15.270/2025) | Secundária |
| FGC | R$ 250 mil por CPF e conglomerado; R$ 1 milhão a cada 4 anos | Secundária |

Não verificado: day trade, compensação de prejuízos, Fiagro, ETF de renda fixa, rendimentos de BDR,
IRPFM e JCP a partir de 2028. Pesquise antes de responder.

Tesouro Direto, debêntures, CRI, CRA, fundos e ações não têm FGC.

## Divergência conhecida no projeto

`application.yml` usa `grace-period-days: 90` para LCI e LCA, citando a CMN 5.119/2024. O prazo
mínimo vigente é 6 meses. Até corrigir, alerte em comparações e não apresente resgate antes disso
como viável para título novo.

## Equivalência

Se `t` é a alíquota do prazo, CDB empata com isento quando:
`taxa bruta do CDB = taxa do isento ÷ (1 − t)`.

É aproximação na taxa; a conta final usa rendimento, IOF, IR e prazo na API de simulação.
