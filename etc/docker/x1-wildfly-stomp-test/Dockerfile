FROM j7beck/x1-wildfly-slot0:8.2.0.Final
MAINTAINER joe <joe@mail.x1>

ADD x1-testing.repo /etc/yum.repos.d/x1-testing.repo
RUN dnf -y install stomp-test-1.2.0 && dnf clean all
ADD collectd.conf /etc/collectd.d/stomp-test-slot0.conf

COPY scripts /srv/wildfly/scripts
RUN chown -R jboss.jboss /srv/wildfly
RUN su -l jboss -c "/srv/wildfly/scripts/configure.sh /srv/wildfly/slot0"
RUN rm -rf /srv/wildfly/scripts

RUN cp /usr/share/java/stomp-test-v1.2.war /srv/wildfly/slot0/deployments/ROOT.war
RUN chown -R jboss.jboss /srv/wildfly

ENV SERVICE_NAME=stomp-test
