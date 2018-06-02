FROM j7beck/x1-wildfly-slot0:13.0.0.Final
MAINTAINER joe <joe@mail.x1>

COPY scripts /srv/wildfly/scripts
COPY maven /srv/wildfly/slot0/deployments
USER root
RUN chown -R jboss.jboss /srv/wildfly
USER jboss:jboss
RUN /srv/wildfly/scripts/configure.sh /srv/wildfly/slot0
RUN rm -rf /srv/wildfly/scripts

ENV SERVICE_NAME=stomp-test DB_USER=stocks DB_PASSWORD=stocks
