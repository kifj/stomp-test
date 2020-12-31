FROM registry.x1/j7beck/x1-postgres
LABEL maintainer="joe@mail.x1"

ADD --chown=postgres sql/ /tmp/sql/
ADD --chown=postgres create-db.sh /tmp/create-db.sh
RUN chmod 755 /tmp/create-db.sh && /tmp/create-db.sh stocks
RUN rm -rf /tmp/create-db.sh /tmp/sql

VOLUME /var/lib/pgsql/data
ENV SERVICE_NAME stomp-test-db
