FROM registry.x1/j7beck/x1-wildfly-profile:21.0.0.Final
LABEL maintainer="joe@mail.x1"

COPY --chown=jboss:jboss scripts /srv/wildfly/scripts
RUN chmod 755 /srv/wildfly/scripts/configure.sh && /srv/wildfly/scripts/configure.sh /srv/wildfly
RUN rm -rf /srv/wildfly/scripts
USER jboss:docker
ENV SERVICE_NAME=stomp-test-it
