-- Carteira simulada (ADR-012, ADR-013). SQL portátil: roda igual no PostgreSQL e no H2 local.
-- portfolio_id já separa as carteiras para quando o login chegar; hoje existe só a de id 1.
CREATE TABLE portfolio
(
  id          BIGINT PRIMARY KEY,
  cash        NUMERIC(19, 2) NOT NULL,
  contributed NUMERIC(19, 2) NOT NULL,
  last_id     BIGINT         NOT NULL
);

CREATE TABLE portfolio_position
(
  portfolio_id  BIGINT         NOT NULL REFERENCES portfolio (id) ON DELETE CASCADE,
  ticker        VARCHAR(12)    NOT NULL,
  name          VARCHAR(200)   NOT NULL,
  asset_class   VARCHAR(20)    NOT NULL,
  quantity      BIGINT         NOT NULL,
  average_price NUMERIC(19, 2) NOT NULL,
  realized_pnl  NUMERIC(19, 2) NOT NULL,
  PRIMARY KEY (portfolio_id, ticker)
);

CREATE TABLE portfolio_execution
(
  portfolio_id BIGINT                   NOT NULL REFERENCES portfolio (id) ON DELETE CASCADE,
  id           BIGINT                   NOT NULL,
  side         VARCHAR(4)               NOT NULL,
  ticker       VARCHAR(12)              NOT NULL,
  quantity     BIGINT                   NOT NULL,
  price        NUMERIC(19, 2)           NOT NULL,
  fees         NUMERIC(19, 2)           NOT NULL,
  total        NUMERIC(19, 2)           NOT NULL,
  executed_at  TIMESTAMP WITH TIME ZONE NOT NULL,
  PRIMARY KEY (portfolio_id, id)
);

CREATE TABLE portfolio_contribution
(
  portfolio_id BIGINT                   NOT NULL REFERENCES portfolio (id) ON DELETE CASCADE,
  seq          INT                      NOT NULL,
  amount       NUMERIC(19, 2)           NOT NULL,
  made_at      TIMESTAMP WITH TIME ZONE NOT NULL,
  PRIMARY KEY (portfolio_id, seq)
);

CREATE TABLE portfolio_fixed_income
(
  portfolio_id      BIGINT         NOT NULL REFERENCES portfolio (id) ON DELETE CASCADE,
  id                BIGINT         NOT NULL,
  product_id        VARCHAR(60)    NOT NULL,
  name              VARCHAR(200)   NOT NULL,
  principal         NUMERIC(19, 2) NOT NULL,
  index_percentage  NUMERIC(9, 4)  NOT NULL,
  income_tax_exempt BOOLEAN        NOT NULL,
  start_date        DATE           NOT NULL,
  PRIMARY KEY (portfolio_id, id)
);
