FROM j7beck/x1-wildfly-slot0:11.0.0.Final
MAINTAINER joe <joe@mail.x1>

ADD collectd.conf /etc/collectd.d/stomp-test-slot0.conf

COPY scripts /srv/wildfly/scripts
COPY maven /srv/wildfly/slot0/deployments
RUN chown -R jboss.jboss /srv/wildfly
RUN su -l jboss -c "/srv/wildfly/scripts/configure.sh /srv/wildfly/slot0"
RUN rm -rf /srv/wildfly/scripts

ENV SERVICE_NAME=stomp-test DB_USER=stocks DB_PASSWORD=stocks
