#!/bin/sh
export MAIL_SERVER=localhost
export MANAGEMENT=local
export JBOSS_BASE_DIR=$1
export JBOSS_HOME=/opt/wildfly
export RUN_CONF=$JBOSS_BASE_DIR/standalone.conf
export JAVA_HOME=/usr/java/default

$JBOSS_HOME/bin/add-user.sh -s -a -u guest -g guest -r ApplicationRealm -sc $JBOSS_BASE_DIR/configuration -p guest_12345!

/usr/local/bin/create-keystore.sh $JBOSS_BASE_DIR/configuration jboss12345 jboss.jboss

$JBOSS_HOME/bin/standalone.sh -c profile-slot0.xml --admin-only &
sleep 30

$JBOSS_HOME/bin/jboss-cli.sh --connect --controller=localhost:9990 --file=$JBOSS_BASE_DIR/../scripts/create-stomp-test.cli
$JBOSS_HOME/bin/jboss-cli.sh --connect --controller=localhost:9990 --command=:shutdown

rm $JBOSS_BASE_DIR/log/*.log $JBOSS_BASE_DIR/configuration/keystore.jks
exit 0
