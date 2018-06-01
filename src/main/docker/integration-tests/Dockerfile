FROM j7beck/x1-wildfly-slot0:13.0.0.Final
MAINTAINER joe <joe@mail.x1>

COPY scripts /srv/wildfly/scripts
RUN /srv/wildfly/scripts/configure.sh /srv/wildfly/slot0
USER root
RUN chown -R jboss.jboss /srv/wildfly
RUN rm -rf /srv/wildfly/scripts
USER jboss:docker
ENV SERVICE_NAME=stomp-test-arquillian
