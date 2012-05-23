This directory contains a "TestClient" application.

This application is NOT documented in any of the Config4J manuals
because its status can be best described as a "partially completed
prototype". The source code of the application is provided for the
benefit of anyone who might like to take on responsibility for
maintaining and extending Config4JMS.

The TestClient application invokes listMessageProducerNames(),
listMessageConsumerNames() and listQueueBrowserNames() operations on the
Config4JMS object to determine the names of all the message producers,
consumers and queue browsers defined in the specified scope of the
configuration file. Then, if the "-producers", "-consumer" and/or
"-queueBrowsers" command-line options are present, the TestClient
creates a separate thread for each of those objects.

	- Each message producer thread goes into an into infinite loop,
	  in which it creates a "Hello, world" text message, prints a
	  configurable diagnostic regarding the message, publishes it,
	  and then sleeps for a period specified by the configuration
	  file.

	- Each message consumer thread blocks to receive messages.
	  When it receives a message, it prints a configurable
	  diagnostic message regarding the received message.

	- Each queue browser thread goes into an infinite loop, in which
	  it prints a configurable diagnostic about the messages on a
	  queue, and then blocks for a configurable amount of time.

The purpose of the TestClient application is to provide a
configurable/scriptable way to test the operation of queues and topics.

Example command-lines (run each in a separate window):

	runtestclient.sh -cfg testclient.cfg -scope test -producers

	runtestclient.sh -cfg testclient.cfg -scope test -consumers

For more details, look at the comments in "testclient.cfg" and at the
source code. It's not difficult to understand, but it is an unfinished
prototype application so there are some rough edges.

