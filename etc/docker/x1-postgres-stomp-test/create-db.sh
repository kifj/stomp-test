#!/bin/sh
DB_NAME=$1
WD=$(dirname $0)

/usr/bin/postgres -D /var/lib/pgsql/data -p 5432 &
sleep 10
createdb --locale=en_US.UTF-8 $DB_NAME || exit 1
psql -d $DB_NAME -f $WD/sql/ddl.sql  || exit 1
psql -d $DB_NAME -f $WD/sql/grant.sql  || exit 1
psql -d $DB_NAME -f $WD/sql/data.sql  || exit 1

kill $(pidof postgres)
exit 0