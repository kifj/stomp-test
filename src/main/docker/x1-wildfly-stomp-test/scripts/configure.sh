#!/bin/sh
export MAIL_SERVER=localhost
export JBOSS_BASE_DIR=$1
export JBOSS_HOME=/opt/wildfly
export JAVA_OPTS="-Djboss.server.base.dir=$JBOSS_BASE_DIR"

cd "$JBOSS_BASE_DIR"
$JBOSS_HOME/bin/add-user.sh -s -a -u guest -g guest -r ApplicationRealm -sc $JBOSS_BASE_DIR/configuration -p "guest_12345!"

/usr/local/bin/create-keystore.sh $JBOSS_BASE_DIR/configuration jboss12345 jboss.jboss
$JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_BASE_DIR/scripts/create-stomp-test.cli

rm -rf $JBOSS_BASE_DIR/log/*.log $JBOSS_BASE_DIR/configuration/keystore.jks $JBOSS_BASE_DIR/configuration/standalone_xml_history/
exit 0
