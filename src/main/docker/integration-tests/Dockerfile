FROM j7beck/x1-wildfly-slot0:11.0.0.Final
MAINTAINER joe <joe@mail.x1>

COPY scripts /srv/wildfly/scripts
RUN chown -R jboss.jboss /srv/wildfly
RUN su -l jboss -c "/srv/wildfly/scripts/configure.sh /srv/wildfly/slot0"
RUN rm -rf /srv/wildfly/scripts

RUN chown -R jboss.jboss /srv/wildfly

ENV SERVICE_NAME=stomp-test-arquillian
