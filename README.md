[![Actions Status](https://github.com/kifj/stomp-test/workflows/Java%20CI/badge.svg)](https://github.com/kifj/stomp-test/actions) ![Licence](https://img.shields.io/github/license/kifj/stomp-test) ![Issues](https://img.shields.io/github/issues/kifj/stomp-test) ![Stars](https://img.shields.io/github/stars/kifj/stomp-test)

# stomp-test

Sample application how to use Wildfly with Websockets and Stomp

[for details](https://blog.johannes-beck.name/?p=285)

## CLI-Scripts for Wildfly

This creates the STOMP acceptor on port 61614

	/socket-binding-group=standard-sockets/socket-binding=messaging-stomp:add(port=61614)
	/subsystem=messaging-activemq/server=default/remote-acceptor=stomp:add(\
		socket-binding=messaging-stomp,\
		params={[name=protocols, key=STOMP]})

The application needs a queue, a topic and a datasource.

	etc/create-stomp-test.cli

## Docker images

Docker images can be built by executing

	mvn -Pdocker clean install

## CLI-Scripts for Wildfly as Bootable JAR

The application can be packaged as a bootable JAR for Wildfly in Docker with

	mvn -Pwildfly-jar clean install

The CLI scripts for this version are located at

	etc/create-wildfly-jar.cli

This versions requires an external ActiveMQ Artemis broker 
which is configured by environment variables:

```
ACTIVEMQ_SERVER (default activemq-artemis)
ACTIVEMQ_PORT (default 61616)
ACTIVEMQ_USER (default artemis)
ACTIVEMQ_PASSWORD (default artemis)
```

## Docker compose

is located in `src/main/docker/stomp-test`

## DDL scripts

The DDL scripts are available for PostgreSQL.

	create-postgresql.sql

## RPM package

RPM package can be built by executing

	mvn -Prpm clean package

## Kubernetes resources

are located in folder `etc/k8s`. The scripts for the certificates require `cfssl`. 

## OpenAPI and manual testing

In Wildfly the OpenAPI spec can be loaded under the `/openapi` URL.
Swagger-UI is located under the application root URL `/stomp-test-v1.8/swagger-ui`.
