#!/bin/sh
DB_NAME=$1
WD=$(dirname $0)

/usr/bin/postgres -D /var/lib/pgsql/data -p 5432 -c "ssl=off" &
sleep 10
createdb --locale=en_US.UTF-8 $DB_NAME || exit 1
psql -d $DB_NAME -f $WD/sql/ddl.sql  || exit 1
psql -d $DB_NAME -f $WD/sql/grant.sql  || exit 1
psql -d $DB_NAME -f $WD/sql/data.sql  || exit 1

kill $(pidof postgres)
rm /var/run/postgresql/.s.PGSQL.5432.lock /var/lib/pgsql/data/postmaster.pid
exit 0
