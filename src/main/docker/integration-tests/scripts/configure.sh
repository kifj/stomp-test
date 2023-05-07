#!/bin/sh
MGMT_USER=admin
MGMT_PASSWORD=12345

export JBOSS_BASE_DIR=$1
export JBOSS_HOME=/opt/wildfly
export RUN_CONF=$WILDFLY_DIR/standalone.conf
export MAIL_SERVER=localhost
export MANAGEMENT=local

$JBOSS_HOME/bin/add-user.sh -s -u $MGMT_USER -r ManagementRealm -sc $JBOSS_BASE_DIR/configuration -p $MGMT_PASSWORD
$JBOSS_HOME/bin/add-user.sh -s -a -u $MGMT_USER -g admin,guest -r ApplicationRealm -sc $JBOSS_BASE_DIR/configuration -p $MGMT_PASSWORD
$JBOSS_HOME/bin/add-user.sh -s -a -u guest -g guest -r ApplicationRealm -sc $JBOSS_BASE_DIR/configuration -p guest_12345!

/usr/local/bin/create-keystore.sh $JBOSS_BASE_DIR/configuration jboss12345 jboss:jboss

$JBOSS_HOME/bin/standalone.sh -c profile.xml --admin-only &
timeout 30 bash -c 'until echo > /dev/tcp/localhost/9990; do sleep 1; done' >& /dev/null || exit -1

$JBOSS_HOME/bin/jboss-cli.sh --connect --controller=localhost:9990 --file=$JBOSS_BASE_DIR/scripts/create-stomp-test.cli
$JBOSS_HOME/bin/jboss-cli.sh --connect --controller=localhost:9990 --command=:shutdown

rm $JBOSS_BASE_DIR/log/*.log $JBOSS_BASE_DIR/configuration/keystore.jks
exit 0
