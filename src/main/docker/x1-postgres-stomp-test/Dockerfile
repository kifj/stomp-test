FROM j7beck/x1-postgres
MAINTAINER joe <joe@mail.x1>

ADD postgresql.conf /etc/collectd.d/postgresql.conf
ADD sql/ /tmp/sql/
ADD create-db.sh /tmp/create-db.sh
RUN chown -R postgres /tmp/create-db.sh /tmp/sql/
RUN su postgres -c "/tmp/create-db.sh stocks"
RUN rm -rf /tmp/create-db.sh /tmp/sql

VOLUME /var/lib/pgsql/data
ENV SERVICE_NAME stomp-test-db
