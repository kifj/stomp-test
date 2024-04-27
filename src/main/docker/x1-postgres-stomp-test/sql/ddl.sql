CREATE SCHEMA stocks;

CREATE TABLE stocks.share (
  id bigint NOT NULL,
  key character varying(25) NOT NULL,
  name character varying(80) NOT NULL,
  version bigint,
  CONSTRAINT share_pkey PRIMARY KEY (id),
  CONSTRAINT idx_key UNIQUE (key)
);

CREATE SEQUENCE stocks.share_seq INCREMENT BY 50;

CREATE INDEX idx_name ON stocks.share (name);
