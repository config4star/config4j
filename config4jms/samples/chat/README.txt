"Chat.java" implements a "chat" application that is commonly used as a
demonstration in JMS books and products.

"ReconnectableChat.java" is a variation on "Chat.java". The only
difference is that it registers an ExceptionListener object, so it
can be notified if it loses its connection to JMS and react by
trying to re-establish the connection.

The "runChat.sh" and "runReconnectableChat.sh" shell scripts can be used
to run the applications. However, you might have to modify the CLASSPATH
settings in those scripts to point to the jar files required to use
whatever JMS product you have.

Use the "chat" scope for topic-based communication. For example:

	runchat.sh -cfg example.cfg -scope chat -u John
	runchat.sh -cfg example.cfg -scope chat -u Mary
	runchat.sh -cfg example.cfg -scope chat -u Anne
	runchat.sh -cfg example.cfg -scope chat -u Frank

Use the "talker.one" and "talker.two" scopes for queue-based
communication. For example:

	runchat.sh -cfg example.cfg -scope talker.two -u John
	runchat.sh -cfg example.cfg -scope talker.two -u Mary

See the comments in "example.cfg" for more details.

