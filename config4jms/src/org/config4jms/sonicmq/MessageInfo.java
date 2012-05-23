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

package org.config4jms.sonicmq;

import java.util.ArrayList;
import java.util.HashMap;

import javax.jms.Destination;
import javax.jms.JMSException;

import org.config4j.Configuration;
import org.config4j.ConfigurationException;
import org.config4j.SchemaValidator;

import org.config4jms.Config4JMSException;

import org.config4jms.base.TypeDefinition;

import progress.message.jclient.Session;
import progress.message.jclient.Message;


public class MessageInfo extends org.config4jms.base.MessageInfo
{
	private Config4JMS			jms;
	private SessionInfo			creator;
	private String				createdBy;
	private Integer				JMSDeliveryMode;
	private Long				JMSExpiration; // milliseconds?
	private Integer				JMSPriority;
	private String				JMSType;
	private String[]			properties;
	private String[]			destinationProperties;


	public MessageInfo(
		org.config4jms.Config4JMS	config4jms,
		String						scope,
		TypeDefinition				typeDef) throws Config4JMSException
	{
		super(config4jms, scope, typeDef);
		jms = (Config4JMS)config4jms;
	}


	public void validateConfiguration() throws Config4JMSException
	{
		String[]				scopeSchema;
		String[]				argScopeSchema;
		SchemaValidator			scopeSv = new SchemaValidator();

		scopeSchema = new String[] {
			"@typedef propType=enum[boolean, string, short, int, long, "
				+ "float, double]",
			"@typedef jmsDeliveryMode=enum["
				+ "NON_PERSISTENT, "
				+ "PERSISTENT, "
				+ "DEFAULT, "
				+ "DISCARDABLE, "
				+ "NON_PERSISTENT_ASYNC, "
				+ "NON_PERSISTENT_REPLICATED, "
				+ "NON_PERSISTENT_SYNC]",
			"createdBy=string",
			"JMSDeliveryMode=jmsDeliveryMode",
			"JMSExpiration=durationMilliseconds",
			"JMSPriority=int[0,9]",
			"JMSType=string",
			"properties=table[string,name, propType,type, string,value]",
			"destinationProperties=table[string,name, string,destination]",
		};

		try {
			//--------
			// Schema check
			//--------
			scopeSv.parseSchema(scopeSchema);
			scopeSv.validate(cfg, scope, "", false,
							 Configuration.CFG_SCOPE_AND_VARS);

			//--------
			// Get the required attributes
			//--------
			createdBy = cfg.lookupString(scope, "createdBy");
		} catch(ConfigurationException ex) {
			throw new Config4JMSException(ex.getMessage());
		}

		//--------
		// Check that cross-references to other scopes are valid.
		//--------
		creator = (SessionInfo)jms.getInfo(createdBy);
		if (creator == null) {
			throw new Config4JMSException(cfg.fileName() + ": value of "
							+ cfg.mergeNames(scope, "createdBy") + " ('"
							+ createdBy + "') does not specify the name of a "
							+ "Session.");
		}

		//--------
		// Get the optional attributes
		//--------
		JMSDeliveryMode = lookupOptionalJMSDeliveryMode("JMSDeliveryMode");
		JMSExpiration = lookupOptionalDurationMillisecondsLong("JMSExpiration");
		JMSPriority = lookupOptionalInt("JMSPriority");
		JMSType = lookupOptionalString("JMSType");
		properties = lookupOptionalList("properties");
		destinationProperties = lookupOptionalList("destinationProperties");
		if (properties == null) {
			properties = new String[0];
		}
		if (destinationProperties == null) {
			destinationProperties = new String[0];
		}
	}


	public void createJMSObject() throws Config4JMSException
	{
	}


	public Message createMessage() throws Config4JMSException
	{
		Message				msg;
		Session				session;

		session = creator.getSession();
		try {
			if (type.equals("Message")) {
				msg = (Message)session.createMessage();
			} else if (type.equals("BytesMessage")) {
				msg = (Message)session.createBytesMessage();
			} else if (type.equals("MapMessage")) {
				msg = (Message)session.createMapMessage();
			} else if (type.equals("MultipartMessage")) {
				msg = (Message)session.createMultipartMessage();
			} else if (type.equals("ObjectMessage")) {
				msg = (Message)session.createObjectMessage();
			} else if (type.equals("StreamMessage")) {
				msg = (Message)session.createStreamMessage();
			} else if (type.equals("TextMessage")) {
				msg = (Message)session.createTextMessage();
			} else {
				msg = (Message)session.createXMLMessage();
			}
		} catch(JMSException ex) {
			throw new Config4JMSException(ex, cfg.fileName() + ": error in "
									+ "creating object for " + scope
									+ "; Session.create" + type + "() failed: "
									+ ex.toString());
		}

		applyMessageProperties(msg);
		return msg;
	}


	public void applyMessageProperties(javax.jms.Message message)
													throws Config4JMSException
	{
		int					i;
		String				pName;
		String				pType;
		String				pValue;
		String				pDest;
		Message				msg;

		msg = (Message)message;
		setAttribute(msg, "JMSDeliveryMode", "setJMSDeliveryMode", int.class,
					 JMSDeliveryMode);
		setAttribute(msg, "JMSExpiration", "setJMSExpiration", long.class,
					 JMSExpiration);
		setAttribute(msg, "JMSPriority", "setJMSPriority", int.class,
					 JMSPriority);
		setAttribute(msg, "JMSType", "setJMSType", String.class, JMSType);

		for (i = 0; i < properties.length; i += 3) {
			pName     = properties[i + 0];
			pType     = properties[i + 1];
			pValue = properties[i + 2];
			try {
				if (pType.equals("string")) {
					msg.setStringProperty(pName, pValue);
				} else if (pType.equals("byte")) {
					msg.setByteProperty(pName, Byte.parseByte(pValue));
				} else if (pType.equals("boolean")) {
					msg.setBooleanProperty(pName, Boolean.parseBoolean(pValue));
				} else if (pType.equals("short")) {
					msg.setShortProperty(pName, Short.parseShort(pValue));
				} else if (pType.equals("int")) {
					msg.setIntProperty(pName, Integer.parseInt(pValue));
				} else if (pType.equals("long")) {
					msg.setLongProperty(pName, Long.parseLong(pValue));
				} else if (pType.equals("float")) {
					msg.setFloatProperty(pName, Float.parseFloat(pValue));
				} else if (pType.equals("double")) {
					msg.setDoubleProperty(pName, Double.parseDouble(pValue));
				}
			} catch(Exception ex) {
				throw new Config4JMSException(cfg.fileName() + ": error in "
					+ "setting property (name='" + pName + "', type='"
					+ type + "', value='" + pValue + "') for " + scope
					+ ": " + ex.toString());
			}
		}

		for (i = 0; i < destinationProperties.length; i += 2) {
			pName = destinationProperties[i + 0];
			pDest = destinationProperties[i + 1];
			try {
				msg.setDestinationProperty(pName,
											config4jms.getDestination(pDest));
			} catch(Exception ex) {
				throw new Config4JMSException(cfg.fileName() + ": error in "
					+ "setting destinationProperty (name='" + pName 
					+ "', destination='" + pDest + "') for " + scope
					+ ": " + ex.toString());
			}
		}
	}


	public Object getObject()
	{
		throw new Error("BUG: MessageInfo.getObject() called!");
	}
}
