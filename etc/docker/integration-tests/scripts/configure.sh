#!/bin/sh
export JBOSS_BASE_DIR=$1
export JAVA_HOME=/usr/java/default
export JBOSS_HOME=/opt/wildfly-10.0.0.Final
export RUN_CONF=$WILDFLY_DIR/standalone.conf
export MAIL_SERVER=localhost
export MANAGEMENT=local
MGMT_USER=admin
MGMT_PASSWORD=12345

$JBOSS_HOME/bin/add-user.sh -s -u $MGMT_USER -r ManagementRealm -sc $JBOSS_BASE_DIR/configuration -p $MGMT_PASSWORD
$JBOSS_HOME/bin/add-user.sh -s -a -u $MGMT_USER -g admin,guest -r ApplicationRealm -sc $JBOSS_BASE_DIR/configuration -p $MGMT_PASSWORD
$JBOSS_HOME/bin/add-user.sh -s -a -u guest -g guest -r ApplicationRealm -sc $JBOSS_BASE_DIR/configuration -p guest_12345!

$JBOSS_HOME/bin/standalone.sh -c profile-slot0.xml &
sleep 20

$JBOSS_HOME/bin/jboss-cli.sh --connect --controller=localhost:9990 --file=$JBOSS_BASE_DIR/../scripts/create-stomp-test.cli

kill -HUP $(pidof java)
sleep 3
rm slot0/log/*.log

exit 0