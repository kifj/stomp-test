FROM j7beck/x1-wildfly-slot0:15.0.1.Final
MAINTAINER joe <joe@mail.x1>

USER root
COPY scripts /srv/wildfly/scripts
RUN /srv/wildfly/scripts/configure.sh /srv/wildfly/slot0

RUN rm -rf /srv/wildfly/scripts
USER jboss:docker
ENV SERVICE_NAME=stomp-test-arquillian
