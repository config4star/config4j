//-----------------------------------------------------------------------
// Copyright 2011 Ciaran McHale.
//
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions.
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.  
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
// BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
// ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
// CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//----------------------------------------------------------------------

package org.config4jms;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.Hashtable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.config4j.Configuration;
import org.config4j.ConfigurationException;

import org.config4jms.base.TypesAndObjects;
import org.config4jms.base.Info;
import org.config4jms.base.MessageInfo;


public abstract class Config4JMS
{
	private String[]				jndiEnvironment;
	private InitialContext			naming;
	protected Configuration			cfg;
	protected String				scope;
	protected TypesAndObjects		typesAndObjects;

	public static Config4JMS create(
		String						cfgSource,
		String						scope,
		Map							cfgPresets) throws Config4JMSException
	{
		Configuration				cfg;
		String						className;
		Class						c;
		Constructor					cons;
		Config4JMS					result = null;
		Iterator					iter;
		Map.Entry					entry;

		try {
			//--------
			// Parse the configuration file and obtain the name of the
			// concrete subclass of Config4JMS.
			//--------
			cfg = Configuration.create();
			if (cfgPresets != null) {
				iter = cfgPresets.entrySet().iterator();
				while (iter.hasNext()) {
					entry = (Map.Entry)iter.next();
					if (entry.getValue() instanceof String) {
						cfg.insertString("", (String)entry.getKey(),
										 (String)entry.getValue());
					} else {
						cfg.insertList("", (String)entry.getKey(),
									   (String[])entry.getValue());
					}
				}
			}
			cfg.parse(cfgSource);
			className = cfg.lookupString(scope, "config4jmsClass",
										 "org.config4jms.portable.Config4JMS");

			//--------
			// Use reflection to create an instance of the subclass.
			//--------
			c = Class.forName(className);
			cons = c.getConstructor(new Class[]{
										Configuration.class, String.class });
			result = (Config4JMS)cons.newInstance(new Object[]{cfg, scope});
		} catch(ConfigurationException ex) {
			throw new Config4JMSException(ex.getMessage());
		} catch(InvocationTargetException ex) {
			throw (Config4JMSException)ex.getTargetException();
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new Config4JMSException(ex.toString());
		}
		return result;
	}


	public static Config4JMS create(
		String						cfgSource,
		String						scope,
		Map							cfgPresets,
		String[]					typeAndNamePairs) throws Config4JMSException
	{
		Config4JMS					jms;

		jms = create(cfgSource, scope, cfgPresets);
		jms.checkObjectsAreDefined(typeAndNamePairs);
		return jms;
	}


	public Configuration getConfiguration()
	{
		return cfg;
	}


	public void createJMSObjects() throws Config4JMSException
	{
		typesAndObjects.createJMSObjects();
	}


	public Message createMessage(String name) throws Config4JMSException
	{
		MessageInfo			msgInfo;

		msgInfo = (MessageInfo)typesAndObjects.getInfo(name);
		return msgInfo.createMessage();
	}


	public void applyMessageProperties(String name, Message msg)
									throws Config4JMSException
	{
		MessageInfo			msgInfo;

		msgInfo = (MessageInfo)typesAndObjects.getInfo(name);
		msgInfo.applyMessageProperties(msg);
	}


	public abstract boolean isNoConnection(JMSException ex);


	public String[] listConnectionFactoryNames()
	{
		return typesAndObjects.listNamesForType("ConnectionFactory");
	}


	public String[] listConnectionNames()
	{
		return typesAndObjects.listNamesForType("Connection");
	}


	public String[] listSessionNames()
	{
		return typesAndObjects.listNamesForType("Session");
	}


	public String[] listDestinationNames()
	{
		return typesAndObjects.listNamesForType("Destination");
	}


	public String[] listQueueNames()
	{
		return typesAndObjects.listNamesForType("Queue");
	}


	public String[] listTemporaryQueueNames()
	{
		return typesAndObjects.listNamesForType("TemporaryQueue");
	}


	public String[] listTopicNames()
	{
		return typesAndObjects.listNamesForType("Topic");
	}


	public String[] listTemporaryTopicNames()
	{
		return typesAndObjects.listNamesForType("TemporaryTopic");
	}


	public String[] listMessageProducerNames()
	{
		return typesAndObjects.listNamesForType("MessageProducer");
	}


	public String[] listMessageConsumerNames()
	{
		return typesAndObjects.listNamesForType("MessageConsumer");
	}


	public String[] listQueueBrowserNames()
	{
		return typesAndObjects.listNamesForType("QueueBrowser");
	}


	public String[] listMessageNames()
	{
		return typesAndObjects.listNamesForType("Message");
	}


	public String[] listBytesMessageNames()
	{
		return typesAndObjects.listNamesForType("ByesMessage");
	}


	public String[] listMapMessageNames()
	{
		return typesAndObjects.listNamesForType("MapMessage");
	}


	public String[] listObjectMessageNames()
	{
		return typesAndObjects.listNamesForType("ObjectMessage");
	}


	public String[] listStreamMessageNames()
	{
		return typesAndObjects.listNamesForType("StreamMessage");
	}


	public String[] listTextMessageNames()
	{
		return typesAndObjects.listNamesForType("TextMessage");
	}


	public void checkObjectsAreDefined(String[] typeAndNamePairs)
													throws Config4JMSException
	{
		int				i;
		int				j;
		boolean			found;
		String			type;
		String			name;
		Object			obj;
		String			namesList[];
		HashMap			map = new HashMap();

		if (typeAndNamePairs.length % 2 != 0) {
			throw new AssertionError("Config4JMS.checkObjectsAreDefined(): "
									 + "the 'typeAndNamePairs' array must "
									 + "contain pairs of (type, name) values");
		}

		for (i = 0; i < typeAndNamePairs.length; i += 2) {
			type = typeAndNamePairs[i + 0];
			name = typeAndNamePairs[i + 1];
			if (!typesAndObjects.isTypeRegistered(type)) {
				throw new AssertionError("Config4JMS.checkObjectsAreDefined(): "
										 + "type '" + type + "' is unknown");
			}
			if (typesAndObjects.getInfo(type, name) == null) {
				throw new Config4JMSException("error: the Config4JMS "
								+ "configuration defined in the '"
								+ scope + "' scope of '" + cfg.fileName()
								+ "' does not define a " + type + " called '"
								+ name + "'");
			}
		}
	}


	public Object getObject(String type, String name) throws Config4JMSException
	{
		Info			info;

		info = (Info)typesAndObjects.getInfo(name);
		if (info != null) {
			return info.getObject();
		}
		throw new Config4JMSException("No " + type + " called '" + name + "'");
	}


	public ConnectionFactory getConnectionFactory(String name)
													throws Config4JMSException
	{
		return (ConnectionFactory)getObject("ConnectionFactory", name);
	}


	public Connection getConnection(String name) throws Config4JMSException
	{
		return (Connection)getObject("Connection", name);
	}


	public Session getSession(String name) throws Config4JMSException
	{
		return (Session)getObject("Session", name);
	}


	public Destination getDestination(String name) throws Config4JMSException
	{
		return (Destination)getObject("Destination", name);
	}


	public Queue getQueue(String name) throws Config4JMSException
	{
		return (Queue)getObject("Queue", name);
	}


	public Queue getTemporaryQueue(String name) throws Config4JMSException
	{
		return (Queue)getObject("Queue", name);
	}


	public Topic getTopic(String name) throws Config4JMSException
	{
		return (Topic)getObject("Topic", name);
	}


	public Topic getTemporaryTopic(String name) throws Config4JMSException
	{
		return (Topic)getObject("Topic", name);
	}


	public MessageProducer getMessageProducer(String name)
								throws Config4JMSException
	{
		return (MessageProducer)getObject("MessageProducer", name);
	}


	public MessageConsumer getMessageConsumer(String name)
								throws Config4JMSException
	{
		return (MessageConsumer)getObject("MessageConsumer", name);
	}


	public QueueBrowser getQueueBrowser(String name) throws Config4JMSException
	{
		return (QueueBrowser)getObject("QueueBrowser", name);
	}


	protected Config4JMS(
		Configuration		cfg,
		String				scope,
		TypesAndObjects		typesAndObjects) throws Config4JMSException
	{
		this.cfg = cfg;
		this.scope = scope;
		this.typesAndObjects = typesAndObjects;
		naming = null;
		jndiEnvironment = cfg.lookupList(scope, "jndiEnvironment",
									new String[0]);

		typesAndObjects.validateConfiguration(this, scope);
	}


	public Object importObjectFromJNDI(String path) throws NamingException
	{
		Object				obj = null;
		Hashtable			env;
		int					i;
		String				name;
		String				value;

		//--------
		// Lazy initialization
		//--------
		if (naming == null) {
			env = new Hashtable();
			for (i = 0; i < jndiEnvironment.length; i+= 2) {
				name  = jndiEnvironment[i+0];
				value = jndiEnvironment[i+1];
				env.put(name, value);
			}
			naming = new InitialContext(env);
		}
		obj = naming.lookup(path);
		return obj;
	}


	public Object importObjectFromFile(String fileName) throws Exception
	{
		FileInputStream		fis = null;
		ObjectInputStream	ois;
		Object				obj = null;

		try {
			fis = new FileInputStream(fileName);
			ois = new ObjectInputStream(fis);
			obj = ois.readObject();
			fis.close();
		} catch(Exception ex) {
			if (fis != null) {
				try {
					fis.close();
				} catch(IOException ex2) {
				}
			}
			throw ex;
		}
		return obj;
	}


	public void startConnections() throws Config4JMSException
	{
		String[]				names;
		int						i;
		Connection				connection;

		names = listConnectionNames();
		for (i = 0; i < names.length; i++) {
			connection = getConnection(names[i]);
			try {
				connection.start();
			} catch(JMSException ex) {
				throw new Config4JMSException(ex,
						"Connection.start() failed for '"
						+ names[i] + "' in " + cfg.fileName() + ": "
						+ ex.toString());
			}
		}
	}


	public void stopConnections() throws Config4JMSException
	{
		String[]				names;
		int						i;
		Connection				connection;

		names = listConnectionNames();
		for (i = 0; i < names.length; i++) {
			connection = getConnection(names[i]);
			try {
				connection.stop();
			} catch(JMSException ex) {
				throw new Config4JMSException(ex,
						"Connection.stop() failed for '"
						+ names[i] + "' in " + cfg.fileName() + ": "
						+ ex.toString());
			}
		}
	}


	public void setExceptionListener(ExceptionListener listener)
													throws Config4JMSException
	{
		String[]				names;
		Connection				connection;
		int						i;

		names = listConnectionNames();
		for (i = 0; i < names.length; i++) {
			connection = getConnection(names[i]);
			try {
				connection.setExceptionListener(listener);
			} catch (JMSException ex) {
				throw new Config4JMSException(ex,
						"Connection.setExceptionListener() failed for '"
						+ names[i] + "' in " + cfg.fileName() + ": "
						+ ex.toString());
			}
		}
	}


	public void closeConnections() throws Config4JMSException
	{
		String[]				names;
		Connection				connection;
		int						i;
		String					problemName = null;
		JMSException			problemEx = null;

		names = listConnectionNames();
		for (i = 0; i < names.length; i++) {
			connection = getConnection(names[i]);
			try {
				if (connection != null) {
					connection.close();
				}
			} catch(JMSException ex) {
				problemName = names[i];
				problemEx = ex;
			}
		}
		if (problemName != null) {
			throw new Config4JMSException(problemEx,
					"Connection.close() failed for '" + problemName
					+ "' in " + cfg.fileName() + ": " + problemEx.toString());
		}
	}

}
