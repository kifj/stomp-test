FROM registry.x1/j7beck/x1-postgres
LABEL maintainer="joe@mail.x1"

USER root
ADD sql/ /tmp/sql/
ADD create-db.sh /tmp/create-db.sh
RUN chown -R postgres /tmp/create-db.sh /tmp/sql/
RUN su postgres -l -s /bin/bash -c "/tmp/create-db.sh stocks"
RUN rm -rf /tmp/create-db.sh /tmp/sql
USER postgres

VOLUME /var/lib/pgsql/data
ENV SERVICE_NAME stomp-test-db
