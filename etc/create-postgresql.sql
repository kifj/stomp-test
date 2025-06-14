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

CREATE TABLE stocks.message (
    id bigint NOT NULL,
    due_date timestamp,
    entry_date timestamp NOT NULL,
    header varchar(1024),
    locked_until timestamp,
    correlation_id varchar(64) NOT NULL,
    payload text,
    queue varchar(64) NOT NULL,
    retries integer NOT NULL,
    priority integer NOT NULL,
    version integer,
    PRIMARY KEY (id),
    CONSTRAINT idx_correlation_id UNIQUE (correlation_id)
);

CREATE index idx_queue ON stocks.message (queue);
CREATE index idx_header ON stocks.message (header);
CREATE index idx_due_date ON stocks.message (due_date);
CREATE index idx_entry_date ON stocks.message (entry_date);
CREATE index idx_locked_until ON stocks.message (locked_until);
CREATE index idx_queue_retries ON stocks.message (queue, retries);
CREATE index idx_priority ON stocks.message (priority);

CREATE SEQUENCE stocks.message_seq INCREMENT BY 50;
ALTER SEQUENCE stocks.message_seq OWNED BY stocks.message.id;
ALTER TABLE stocks.message ALTER COLUMN id SET DEFAULT nextval('stocks.message_seq');

CREATE TABLE stocks.configuration (
    key varchar(255) NOT NULL,
    value varchar(255),
    PRIMARY KEY (key)
);

INSERT INTO stocks.configuration VALUES ('message-queue.numberOfMessagesToAcquire', '20');
INSERT INTO stocks.configuration VALUES ('message-queue.waitIfNoMessagesAvailable', '1000');
INSERT INTO stocks.configuration VALUES ('message-queue.logStacktraceIfReceiveFailed', 'false');
INSERT INTO stocks.configuration VALUES ('message-queue.refreshConfigurationInterval', '60');