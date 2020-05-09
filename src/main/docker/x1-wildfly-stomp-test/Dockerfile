FROM registry.x1/j7beck/x1-wildfly-profile:19.1.0.Final
MAINTAINER joe <joe@mail.x1>

USER root
COPY scripts /srv/wildfly/scripts
COPY maven /srv/wildfly/deployments
RUN /srv/wildfly/scripts/configure.sh /srv/wildfly
RUN rm -rf /srv/wildfly/scripts
USER jboss:docker
ENV SERVICE_NAME=stomp-test DB_USER=stocks DB_PASSWORD=stocks
