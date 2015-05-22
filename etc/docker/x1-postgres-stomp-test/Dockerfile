FROM j7beck/x1-postgres
MAINTAINER joe <joe@mail.x1>

ADD sql/ /tmp/sql/
ADD create-db.sh /tmp/create-db.sh
ADD postgresql.conf /etc/collectd.d/postgresql.conf
RUN su postgres -c "/tmp/create-db.sh stocks"
RUN rm -rf /tmp/create-db.sh /tmp/sql

ENV SERVICE_NAME stomp-test-db
