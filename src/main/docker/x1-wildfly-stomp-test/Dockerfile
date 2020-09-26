FROM registry.x1/j7beck/x1-wildfly-profile:20.0.0.Final
LABEL maintainer="joe@mail.x1"

USER root
COPY scripts /srv/wildfly/scripts
COPY ROOT.war /srv/wildfly/deployments/ROOT.war
RUN chmod 755 /srv/wildfly/scripts/configure.sh && /srv/wildfly/scripts/configure.sh /srv/wildfly
RUN rm -rf /srv/wildfly/scripts
USER jboss:docker
ENV SERVICE_NAME=stomp-test DB_USER=stocks DB_PASSWORD=stocks
