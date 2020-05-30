# stomp-test

Sample application how to use Wildfly with Websockets and Stomp

See details: [http://blog.johannes-beck.name/?p=285](http://blog.johannes-beck.name/?p=285)

## CLI-Scripts for Wildfly

This creates the STOMP acceptor on port 61614

	/socket-binding-group=standard-sockets/socket-binding=messaging-stomp:add(port=61614)
	/subsystem=messaging-activemq/server=default/remote-acceptor=stomp:add(\
		socket-binding=messaging-stomp,\
		params={[name=protocols, key=STOMP]})

The application needs a queue, a topic and a datasource.

	etc/create-stomp-test.cli

## DDL scripts

The DDL scripts are available for PostgreSQL.

	create-postgresql.cli

## RPM package

RPM package can be built by executing

	mvn -Prpm clean package

## Docker images

Docker images can be built by executing

	mvn -Pdocker clean install

## Kubernetes resources

are located in folder etc/k8s