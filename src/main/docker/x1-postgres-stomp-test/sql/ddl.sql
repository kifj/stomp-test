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

CREATE TABLE stocks.jboss_ejb_timer (
<<<<<<< HEAD
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
=======
  ID VARCHAR PRIMARY KEY NOT NULL,
  TIMED_OBJECT_ID VARCHAR NOT NULL,
  INITIAL_DATE TIMESTAMP,
  REPEAT_INTERVAL BIGINT,
  NEXT_DATE TIMESTAMP,
  PREVIOUS_RUN TIMESTAMP,
  PRIMARY_KEY VARCHAR,
  INFO TEXT,
  TIMER_STATE VARCHAR,
  SCHEDULE_EXPR_SECOND VARCHAR,
  SCHEDULE_EXPR_MINUTE VARCHAR,
  SCHEDULE_EXPR_HOUR VARCHAR,
  SCHEDULE_EXPR_DAY_OF_WEEK VARCHAR,
  SCHEDULE_EXPR_DAY_OF_MONTH VARCHAR,
  SCHEDULE_EXPR_MONTH VARCHAR,
  SCHEDULE_EXPR_YEAR VARCHAR,
  SCHEDULE_EXPR_START_DATE VARCHAR,
  SCHEDULE_EXPR_END_DATE VARCHAR,
  SCHEDULE_EXPR_TIMEZONE VARCHAR,
  AUTO_TIMER BOOLEAN,
  TIMEOUT_METHOD_DECLARING_CLASS VARCHAR,
  TIMEOUT_METHOD_NAME VARCHAR,
  TIMEOUT_METHOD_DESCRIPTOR VARCHAR,
  CALENDAR_TIMER BOOLEAN,
  PARTITION_NAME VARCHAR NOT NULL,
  NODE_NAME VARCHAR);

CREATE INDEX JBOSS_EJB_TIMER_IDENX ON stocks.jboss_ejb_timer (PARTITION_NAME, TIMED_OBJECT_ID);
>>>>>>> c532e9edc459efe308697aefb345700fc594605a
