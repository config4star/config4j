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

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;


public class MessageListener
    implements javax.jms.MessageListener
{
	private Config4JMS			jms;
	private Configuration		cfg;
	private String				scope;
	private String				defaultScope;
	private String				consumerName;
	private String				messagePrefixFormat;
	private MessageFormatter	msgFormatter;


	public MessageListener(
		Config4JMS				jms,
		Configuration			cfg,
		String					rootScope,
		String					consumerName,
		MessageFormatter		msgFormatter) throws ConfigurationException
	{
		this.jms = jms;
		this.cfg = cfg;
		this.consumerName = consumerName;
		this.scope = cfg.mergeNames(rootScope, consumerName);
		this.defaultScope = cfg.mergeNames(rootScope,"default.MessageConsumer");
		this.msgFormatter = msgFormatter;

		messagePrefixFormat = lookupString("messagePrefixFormat",
						"\n${consumerName} received ${msgType} at ${time}\n");
	}


	protected String lookupString(String name, String defaultValue)
												throws ConfigurationException
	{
		String					result;

		if (cfg.type(scope, name) != Configuration.CFG_NO_VALUE) {
			result = cfg.lookupString(scope, name);
		} else {
			result = cfg.lookupString(defaultScope, name, defaultValue);
		}
		return result;
	}


	public void onMessage(Message msg)
	{
		String				msgType;
		String				messagePrefix;
		HashMap				messagePrefixMap = new HashMap();
		String				time;

		time = new Date().toString();
		msgType = msg.getClass().getName();
		msgType = msgType.substring(msgType.lastIndexOf('.') + 1);
		messagePrefixMap.put("${consumerName}", this.consumerName);
		messagePrefixMap.put("${msgType}", msgType);
		messagePrefixMap.put("${time}", time);
		messagePrefix = Util.replace(messagePrefixFormat, messagePrefixMap);
		System.out.println(messagePrefix + msgFormatter.format(msg));
    }

}
