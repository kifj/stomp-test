FROM registry.x1/j7beck/x1-wildfly-profile:10.1.0.Final
MAINTAINER joe <joe@mail.x1>

USER root
COPY scripts /srv/wildfly/scripts
RUN /srv/wildfly/scripts/configure.sh /srv/wildfly
RUN chown -R jboss.jboss /srv/wildfly
RUN rm -rf /srv/wildfly/scripts
USER jboss:docker
ENV SERVICE_NAME=stomp-test-arquillian
