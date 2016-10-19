CREATE SCHEMA stocks;

CREATE SEQUENCE stocks.hibernate_sequence;

CREATE TABLE stocks.share (
  id bigint NOT NULL,
  key character varying(25) NOT NULL,
  name character varying(80) NOT NULL,
  version bigint,
  CONSTRAINT share_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_key ON stocks.share (key);
CREATE INDEX idx_name ON stocks.share (name);
