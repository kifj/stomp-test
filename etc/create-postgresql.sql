CREATE SCHEMA stocks;

CREATE TABLE stocks.share (
  id bigint NOT NULL,
  key character varying(25) NOT NULL,
  name character varying(80) NOT NULL,
  version bigint,
  CONSTRAINT share_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_key ON stocks.share (key);
CREATE INDEX idx_name ON stocks.share (name);
CREATE SEQUENCE stocks.hibernate_sequence;

CREATE TABLE stocks.jboss_ejb_timer (
  id VARCHAR PRIMARY KEY NOT NULL,
  timed_object_id VARCHAR NOT NULL,
  initial_date TIMESTAMP,
  repeat_interval BIGINT,
  next_date TIMESTAMP,
  previous_run TIMESTAMP,
  primary_key VARCHAR,
  info TEXT,
  timer_state VARCHAR,
  schedule_expr_second VARCHAR,
  schedule_expr_minute VARCHAR,
  schedule_expr_hour VARCHAR,
  schedule_expr_day_of_week VARCHAR,
  schedule_expr_day_of_month VARCHAR,
  schedule_expr_month VARCHAR,
  schedule_expr_year VARCHAR,
  schedule_expr_start_date VARCHAR,
  schedule_expr_end_date VARCHAR,
  schedule_expr_timezone VARCHAR,
  auto_timer BOOLEAN,
  timeout_method_declaring_class VARCHAR,
  timeout_method_name VARCHAR,
  timeout_method_descriptor VARCHAR,
  calendar_timer BOOLEAN,
  partition_name VARCHAR NOT NULL,
  node_name VARCHAR);

CREATE INDEX jboss_ejb_timer_index ON stocks.jboss_ejb_timer (partition_name, timed_object_id);
