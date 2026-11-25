-- Mesmo registro de eventos do V1 do PostgreSQL, nos tipos do H2 usado pelo perfil local.
CREATE TABLE event_publication
(
  id                     UUID NOT NULL,
  listener_id            VARCHAR(512) NOT NULL,
  event_type             VARCHAR(512) NOT NULL,
  serialized_event       VARCHAR(4000) NOT NULL,
  publication_date       TIMESTAMP(9) WITH TIME ZONE NOT NULL,
  completion_date        TIMESTAMP(9) WITH TIME ZONE,
  status                 VARCHAR(20),
  completion_attempts    INT,
  last_resubmission_date TIMESTAMP(9) WITH TIME ZONE,
  PRIMARY KEY (id)
);

CREATE INDEX event_publication_by_completion_date_idx ON event_publication (completion_date);
