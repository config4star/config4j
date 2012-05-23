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

package org.config4jms.portable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.config4j.ConfigurationException;
import org.config4jms.Config4JMSException;

import org.config4jms.base.TypeDefinition;
import org.config4jms.base.TypesAndObjects;


public class PortableTypesAndObjects extends TypesAndObjects
{
	public PortableTypesAndObjects()
	{
		super(new TypeDefinition[]{
			new TypeDefinition("ConnectionFactory", null,
					"org.config4jms.portable.ConnectionFactoryInfo"),
			new TypeDefinition("Connection", null,
					"org.config4jms.portable.ConnectionInfo"),
			new TypeDefinition( "Session", null,
					"org.config4jms.portable.SessionInfo"),
			new TypeDefinition("Destination", null, null), // abstract
			new TypeDefinition("Queue", new String[]{"Destination"},
					"org.config4jms.portable.QueueInfo"),
			new TypeDefinition("TemporaryQueue", new String[]{"Destination"},
					"org.config4jms.portable.TemporaryQueueInfo"),
			new TypeDefinition("Topic", new String[]{"Destination"},
					"org.config4jms.portable.TopicInfo"),
			new TypeDefinition("TemporaryTopic", new String[]{"Destination"},
					"org.config4jms.portable.TemporaryTopicInfo"),
			new TypeDefinition("MessageConsumer", null,
					"org.config4jms.portable.MessageConsumerInfo"),
			new TypeDefinition("MessageProducer", null,
					"org.config4jms.portable.MessageProducerInfo"),
			new TypeDefinition("QueueBrowser", null,
					"org.config4jms.portable.QueueBrowserInfo"),
			new TypeDefinition("TopicSubscriber",
					new String[]{"MessageConsumer"},
					"org.config4jms.portable.TopicSubscriberInfo"),
			new TypeDefinition("Message", null,
					"org.config4jms.portable.MessageInfo"),
			new TypeDefinition("BytesMessage", new String[]{"Message"},
					"org.config4jms.portable.MessageInfo"),
			new TypeDefinition("MapMessage", new String[]{"Message"},
					"org.config4jms.portable.MessageInfo"),
			new TypeDefinition("ObjectMessage", new String[]{"Message"},
					"org.config4jms.portable.MessageInfo"),
			new TypeDefinition("StreamMessage", new String[]{"Message"},
					"org.config4jms.portable.MessageInfo"),
			new TypeDefinition("TextMessage", new String[]{"Message"},
					"org.config4jms.portable.MessageInfo"),
		});
	}

}
