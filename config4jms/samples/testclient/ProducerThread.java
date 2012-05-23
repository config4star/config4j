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

package testclient;

import org.config4j.Configuration;
import org.config4j.ConfigurationException;

import org.config4jms.Config4JMS;
import org.config4jms.Config4JMSException;

import java.util.Date;
import java.util.HashMap;
import java.util.Enumeration;

import javax.jms.Session;
import javax.jms.MessageProducer;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;


public class ProducerThread extends Thread
{
	private Config4JMS			jms;
	private Configuration		cfg;
	private Session				session;
	private String				scope;
	private String				defaultScope;
	private String				producerName;
	private MessageProducer		producer;
	private String				messagePrefixFormat;
	private int					pause;
	private String				messageName;
	private MessageFormatter	msgFormatter;


	public ProducerThread(
		Config4JMS				jms,
		Configuration			cfg,
		String					rootScope,
		String					producerName,
		MessageFormatter		msgFormatter) throws Exception
	{
		this.jms = jms;
		this.cfg = cfg;
		this.producerName = producerName;
		this.scope = cfg.mergeNames(rootScope, producerName);
		producer = jms.getMessageProducer(producerName);
		defaultScope = cfg.mergeNames(rootScope,"default.MessageProducer");
		this.msgFormatter = msgFormatter;
		messagePrefixFormat = lookupString("messagePrefixFormat",
					"\nMessageProducer '${producerName}' sending ${msgType}\n");
		pause = lookupDurationMilliseconds("pause", 2 * 1000); // 2 seconds
		messageName = lookupString("messageName", "");
		if (!messageName.equals("")) {
			jms.createMessage(messageName);
		}
		session = jms.getSession(jms.listSessionNames()[0]);
	}


	protected int lookupDurationMilliseconds(
		String				name,
		int					defaultValue) throws ConfigurationException
	{
		int					result;

		if (cfg.type(scope, name) != Configuration.CFG_NO_VALUE) {
			result = cfg.lookupDurationMilliseconds(scope, name);
		} else {
			result = cfg.lookupDurationMilliseconds(defaultScope, name,
										defaultValue);
		}
		return result;
	}

	protected String lookupString(String name, String defaultValue)
												throws ConfigurationException
	{
		String				result;

		if (cfg.type(scope, name) != Configuration.CFG_NO_VALUE) {
			result = cfg.lookupString(scope, name);
		} else {
			result = cfg.lookupString(defaultScope, name, defaultValue);
		}
		return result;
	}


	public void run()
	{
		Message				msg;

		while (true) {
			try {
				msg = createMessage();
				printMessageDiagnostics(msg);
				producer.send(msg);
				Thread.sleep(pause);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}


	protected Message createMessage() throws JMSException
	{
		Message				msg;

		if (messageName.equals("")) {
			msg = session.createTextMessage();
		} else {
			msg = jms.createMessage(messageName);
		}
		((TextMessage)msg).setText("Hello, world");
		return msg;
	}


	protected void printMessageDiagnostics(Message msg)
	{
		String				time;
		String				msgType;
		HashMap				messagePrefixMap = new HashMap();
		String				messagePrefix;

		time = new Date().toString();
		msgType = msg.getClass().getName();
		msgType = msgType.substring(msgType.lastIndexOf('.') + 1);
		messagePrefixMap.put("${producerName}", this.producerName);
		messagePrefixMap.put("${msgType}", msgType);
		messagePrefixMap.put("${time}", time);
		messagePrefix = Util.replace(messagePrefixFormat, messagePrefixMap);
		System.out.println(messagePrefix + msgFormatter.format(msg));
    }

}
