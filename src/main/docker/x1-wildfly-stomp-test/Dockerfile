FROM registry.x1/j7beck/x1-wildfly-profile:31.0.1.Final
LABEL maintainer="joe@mail.x1"

COPY --chown=jboss:jboss scripts /srv/wildfly/scripts
COPY --chown=jboss:jboss ROOT.war /srv/wildfly/deployments/ROOT.war
RUN chmod 755 /srv/wildfly/scripts/configure.sh && /srv/wildfly/scripts/configure.sh /srv/wildfly && rm -rf /srv/wildfly/scripts

ENV SERVICE_NAME=stomp-test DB_USER=stocks DB_PASSWORD=stocks
