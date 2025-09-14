CREATE SCHEMA stocks;

CREATE TABLE stocks.share (
  id bigint NOT NULL,
  key varchar(25) NOT NULL,
  name varchar(80) NOT NULL,
  version bigint,
  CONSTRAINT share_pkey PRIMARY KEY (id),
  CONSTRAINT idx_key UNIQUE (key)
);

CREATE SEQUENCE stocks.share_seq INCREMENT BY 50;
ALTER SEQUENCE stocks.share_seq OWNED BY stocks.share.id;
ALTER TABLE stocks.share ALTER COLUMN id SET DEFAULT nextval('stocks.share_seq');

CREATE INDEX idx_name ON stocks.share (name);
