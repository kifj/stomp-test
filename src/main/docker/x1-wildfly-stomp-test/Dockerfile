FROM registry.x1/j7beck/x1-wildfly-slot0:18.0.1.Final
MAINTAINER joe <joe@mail.x1>

USER root
COPY scripts /srv/wildfly/scripts
COPY maven /srv/wildfly/slot0/deployments
RUN /srv/wildfly/scripts/configure.sh /srv/wildfly/slot0
RUN rm -rf /srv/wildfly/scripts
USER jboss:docker
ENV SERVICE_NAME=stomp-test DB_USER=stocks DB_PASSWORD=stocks
