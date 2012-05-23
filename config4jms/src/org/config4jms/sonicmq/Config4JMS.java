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

import javax.jms.JMSException;

import org.config4j.Configuration;

import org.config4jms.Config4JMSException;

import progress.message.jclient.ErrorCodes;


public class Config4JMS extends org.config4jms.portable.Config4JMS
{
	public Config4JMS(
		Configuration		cfg,
		String				scope) throws Config4JMSException
	{
		super(cfg, scope, new SonicMQTypesAndObjects());
	}


	public boolean isNoConnection(JMSException ex)
	{
		String[]			keywords;
		String				str;
		int					i;

		if (ErrorCodes.testException(ex, ErrorCodes.ERR_CONNECTION_DROPPED)) {
			return true;
		}

		keywords = new String[]{
			"com.sonicsw.mf.comm.InvokeTimeoutCommsException",
			"com.sonicsw.mf.comm.ConnectTimeoutException",
		};
		str = ex.toString();
		for (i = 0; i < keywords.length; i++) {
			if (str.indexOf(keywords[i]) != -1) {
				return true;
			}
		}
		return super.isNoConnection(ex);
	}


	Info getInfo(String name) throws Config4JMSException
	{
		return (Info)typesAndObjects.getInfo(name);
	}

}
