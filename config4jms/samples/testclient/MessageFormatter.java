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
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageFormatException;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;


public class MessageFormatter
{
	Configuration				cfg;
	private boolean				suppressNullHeaders;
	private String[]			headersToPrint;
	private String				headerPrintFormat;
	private String[]			propertiesToPrint;
	private String				propertyPrintFormat;
	private String				outputFormat;
	private String				scope;

	public MessageFormatter(
		Configuration			cfg,
		String					scope) throws ConfigurationException
	{
		this.cfg = cfg;
		this.scope = scope;

		suppressNullHeaders = cfg.lookupBoolean(scope, "suppressNullHeaders",
												false);
		headersToPrint = cfg.lookupList(scope, "headersToPrint",
											new String[]{"*"});
		headerPrintFormat = cfg.lookupString(scope, "headerPrintFormat",
										"${name} = ${value}\n");
		propertiesToPrint = cfg.lookupList(scope, "headersToPrint",
											new String[]{"*"});
		propertyPrintFormat = cfg.lookupString(scope, "propertyPrintFormat",
										"${name} = ${value}\n");
		outputFormat = cfg.lookupString(scope, "outputFormat",
										"----headers----\n"
										+ "${headers}"
										+ "----properties----\n"
										+ "${properties}"
										+ "----body----\n"
										+ "${body}\n"
										+ "---end of ${msgType}----\n");
	}


	public String format(Message msg)
	{
		String				msgType;
		HashMap				outputFormatMap = new HashMap();
		Date				now = new Date();
		String				headers;
		String				properties = "";
		String				body = "<dummy body>";
		String				str;

		msgType = msg.getClass().getName();
		msgType = msgType.substring(msgType.lastIndexOf('.') + 1);
		headers = getHeadersAsString(msg);
		properties = getPropertiesAsString(msg);
		body = getBodyAsString(msg, msgType);

		outputFormatMap.put("${msgType}", msgType);
		outputFormatMap.put("${headers}", headers);
		outputFormatMap.put("${properties}", properties);
		outputFormatMap.put("${body}", body);
		str = Util.replace(outputFormat, outputFormatMap);
		return str;
    }


	protected String getBodyAsString(Message msg, String msgType)
	{
		BytesMessage		bytesMsg;
		byte[]				data;
		int					i;
		int					size;

        try {
			if (msg instanceof TextMessage) {
				 return ((TextMessage)msg).getText();
			} else if (msg instanceof StreamMessage) {
				return getStreamMessageBodyAsString((StreamMessage)msg);
			} else if (msg instanceof ObjectMessage) {
				return ((ObjectMessage)msg).getObject().toString();
			} else if (msg instanceof BytesMessage) {
				bytesMsg = (BytesMessage)msg;
				size = (int)bytesMsg.getBodyLength();
				data = new byte[size];
				for (i = 0; i < size; i++) {
					data[i] = bytesMsg.readByte();
				}
				return getBytesAsString(data);
			} else if (msg instanceof MapMessage) {
				return getMapMessageBodyAsString((MapMessage)msg);
			}
		} catch(JMSException ex) {
			ex.printStackTrace();
        } catch (java.lang.RuntimeException ex) {
            ex.printStackTrace();
        }
		return "";
	}


	protected String getStreamMessageBodyAsString(StreamMessage msg)
	{
		return "";
	}


	protected String getMapMessageBodyAsString(MapMessage msg)
	{
		int						i;
		StringBuffer			buf = new StringBuffer();
		String					name;
		String					value;
		Enumeration				e;

		i = 0;
		try {
				e = msg.getMapNames();
		} catch (JMSException ex) {
			return "error: " + ex.toString();
		}
		for (; e.hasMoreElements();) {
			name = (String)e.nextElement();
			try {
				try {
					value = msg.getString(name);
				} catch(MessageFormatException ex) {
					value = getBytesAsString(msg.getBytes(name));
				}
			} catch(Exception ex) {
				value = "error: " + ex.toString();
			}
			buf.append(formatProperty(name, value));
		}
		return buf.toString();
	}


	protected String getBytesAsString(byte[] data)
	{
		int					i;
		StringBuffer		buf = new StringBuffer();

		buf.append("data contains " + data.length + " bytes\n");
		return buf.toString();
	}


	protected String getJMSDeliveryModeAsString(Message msg)
	{
		int			mode;
		
		try {
			mode = msg.getJMSDeliveryMode();
		} catch (JMSException ex) {
			return "error: " + ex.toString();
		}

		if (mode == DeliveryMode.NON_PERSISTENT) {
			return "NON_PERSISTENT (" + mode + ")";
		} else if (mode == DeliveryMode.PERSISTENT) {
			return "PERSISTENT (" + mode + ")";
		} else {
			return "unknown proprietary value (" + mode + ")";
		}
	}


	protected String getHeadersAsString(Message msg)
	{
		long					expiration;
		boolean					redelivered;
		StringBuffer			buf = new StringBuffer();
		String					str;
		Destination				dest;

		try {
			if (wantHeader("JMSCorrelationID")) {
				str = msg.getJMSCorrelationID();
				if (str != null || !suppressNullHeaders) {
					buf.append(formatHeader("JMSCorrelationID", str));
				}
			}
			if (wantHeader("JMSDestination")) {
				dest = msg.getJMSDestination();
				if (dest != null || !suppressNullHeaders) {
					if (dest == null) {
						str = null;
					} else {
						str = dest.toString();
					}
					buf.append(formatHeader("JMSDestination", str));
				}
			}
			if (wantHeader("JMSDeliveryMode")) {
				buf.append(formatHeader("JMSDeliveryMode",
									getJMSDeliveryModeAsString(msg)));
			}
			if (wantHeader("JMSExpiration")) {
				expiration = msg.getJMSExpiration();
				if (expiration != 0 || !suppressNullHeaders) {
					buf.append(formatHeader("JMSExpiration", "" + expiration));
				}
			}
			if (wantHeader("JMSMessageID")) {
				buf.append(formatHeader("JMSMessageID",
									"" + msg.getJMSMessageID()));
			}
			if (wantHeader("JMSPriority")) {
				buf.append(formatHeader("JMSPriority",
									"" + msg.getJMSPriority()));
			}
			if (wantHeader("JMSTimestamp")) {
				buf.append(formatHeader("JMSTimestamp",
									"" + msg.getJMSTimestamp()));
			}
			if (wantHeader("JMSReplyTo")) {
				dest = msg.getJMSReplyTo();
				if (dest != null || !suppressNullHeaders) {
					if (dest == null) {
						str = null;
					} else {
						str = dest.toString();
					}
					buf.append(formatHeader("JMSReplyTo", str));
				}
			}
			if (wantHeader("JMSType")) {
				str = msg.getJMSType();
				if (str != null || !suppressNullHeaders) {
					buf.append(formatHeader("JMSType", str));
				}
			}
			if (wantHeader("JMSRedelivered")) {
				redelivered = msg.getJMSRedelivered();
				if (redelivered || !suppressNullHeaders) {
					buf.append(formatHeader("JMSRedelivered",
									"" + redelivered));
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
			return "Error getting message headers: " + ex.toString();
		}
		return buf.toString();
	}


	protected String getPropertiesAsString(Message msg)
	{
		StringBuffer			buf = new StringBuffer();
		String					name;
		String					value;
		Enumeration				e;

		try {
			e = msg.getPropertyNames();
			if (e == null) {
				return "";
			}
			for (; e.hasMoreElements();) {
				name = (String)e.nextElement();
				value = msg.getStringProperty(name);
				buf.append(formatProperty(name, value));
			}
		} catch(Exception ex) {
			return "Error getting message properties: " + ex.toString();
		}
		return buf.toString();
	}


	protected boolean wantHeader(String name)
	{
		return Util.isStringInListOfPatterns(name, headersToPrint);
	}


	protected String formatHeader(String name, String value)
	{
		HashMap			formatMap = new HashMap();

		if (value == null) { value = "null"; }
		formatMap.put("${name}", name);
		formatMap.put("${value}", value);
		return Util.replace(headerPrintFormat, formatMap);
	}


	protected String formatProperty(String name, String value)
	{
		HashMap			formatMap = new HashMap();

		if (value == null) { value = "null"; }
		formatMap.put("${name}", name);
		formatMap.put("${value}", value);
		return Util.replace(propertyPrintFormat, formatMap);
	}

}
