# stomp-test

Sample application how to use JBoss Wildfly with Websockets and Stomp

See details: [http://blog.johannes-beck.name/?p=285](http://blog.johannes-beck.name/?p=285)

## CLI-Scripts for Wildfly 10.0

This creates the STOMP acceptor on port 61614

	/socket-binding-group=standard-sockets/socket-binding=messaging-stomp:add(port=61614)
	/subsystem=messaging-activemq/server=default/remote-acceptor=stomp:add(\
		socket-binding=messaging-stomp,\
		params={[name=protocols, key=STOMP]})

The application needs a queue, a topic and a datasource.

	etc\docker\x1-wildfly-stomp-test\scripts\create-stomp-test.cli
	
