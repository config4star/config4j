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

import javax.jms.QueueBrowser;
import javax.jms.JMSException;
import javax.jms.Message;


public class QueueBrowserThread extends Thread
{
	private Config4JMS			jms;
	private Configuration		cfg;
	private String				scope;
	private String				defaultScope;
	private String				qBrowserName;
	private String				queueName;
	private String				messageSelector;
	private QueueBrowser		qBrowser;
	private String				summaryFormat;
	private int					pause;
	private MessageFormatter	msgFormatter;


	public QueueBrowserThread(
		Config4JMS				jms,
		Configuration			cfg,
		String					rootScope,
		String					qBrowserName,
		MessageFormatter		msgFormatter) throws Exception
	{
		this.jms = jms;
		this.cfg = cfg;
		this.qBrowserName = qBrowserName;
		this.scope = cfg.mergeNames(rootScope, qBrowserName);
		qBrowser = jms.getQueueBrowser(qBrowserName);
		queueName = qBrowser.getQueue().getQueueName();
		messageSelector = qBrowser.getMessageSelector();
		defaultScope = cfg.mergeNames(rootScope,"default.QueueBrowser");
		this.msgFormatter = msgFormatter;
		summaryFormat = lookupString("summaryFormat",
				"\nQueueBrowser '${queueBrowserName}' ${count} messages\n");
		pause = lookupDurationMilliseconds("pause", 2 * 1000); // 2 seconds
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
		while (true) {
			try {
				printQueueReport();
				Thread.sleep(pause);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}


	protected void printQueueReport() throws Exception
	{
		int					count;
		Enumeration			e;
		Message				msg;
		String				time;
		HashMap				summaryFormatMap = new HashMap();
		String				summary;

		count = 0;
		for (e = qBrowser.getEnumeration(); e.hasMoreElements();) {
			count++;
			msg = (Message)e.nextElement();
			System.out.print(msgFormatter.format(msg));
		}

		time = new Date().toString();
		summaryFormatMap.put("${queueBrowserName}", this.qBrowserName);
		summaryFormatMap.put("${queueName}", queueName);
		summaryFormatMap.put("${messageSelector}", messageSelector);
		summaryFormatMap.put("${time}", time);
		summaryFormatMap.put("${count}", "" + count);
		summary = Util.replace(summaryFormat, summaryFormatMap);
		System.out.print(summary);
    }

}
