# stomp-test

Sample application how to use JBoss Wildfly with Websockets and Stomp

See details: [http://blog.johannes-beck.name/?p=285](http://blog.johannes-beck.name/?p=285)

<<<<<<< HEAD
## CLI-Scripts for Wildfly 10.0
=======
## CLI-Scripts for Wildfly
>>>>>>> 1115c8d... Small refactorings

This creates the STOMP acceptor on port 61614

	/socket-binding-group=standard-sockets/socket-binding=messaging-stomp:add(port=61614)
	/subsystem=messaging-activemq/server=default/remote-acceptor=stomp:add(\
		socket-binding=messaging-stomp,\
		params={[name=protocols, key=STOMP]})

The application needs a queue, a topic and a datasource.

	etc/create-stomp-test.cli
	
