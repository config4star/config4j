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

import javax.jms.JMSException;
import org.config4j.Configuration;
import org.config4jms.Config4JMSException;
import org.config4jms.base.TypesAndObjects;


public class Config4JMS extends org.config4jms.Config4JMS
{

	public Config4JMS(
		Configuration		cfg,
		String				scope) throws Config4JMSException
	{
		super(cfg, scope, new PortableTypesAndObjects());
	}


	//--------
	// The org.config4jms.sonicmq.Config4JMS class inherits from
	// this one so it can reuse the isNoConnection() operation.
	// It is that subclass that calls the following constructor.
	//--------
	protected Config4JMS(
		Configuration		cfg,
		String				scope,
		TypesAndObjects		typesAndObjects) throws Config4JMSException
	{
		super(cfg, scope, typesAndObjects);
	}


	public boolean isNoConnection(JMSException ex)
	{
		String[]			keywords;
		String				str;
		int					i;

		keywords = new String[]{
			"java.net.SocketException",
			"java.net.ConnectException",
			"java.net.NoRouteToHostException",
			"Connection refused",
		};
		str = ex.toString();
		for (i = 0; i < keywords.length; i++) {
			if (str.indexOf(keywords[i]) != -1) {
				return true;
			}
		}
		return false;
	}


	Info getInfo(String name) throws Config4JMSException
	{
		return (Info)typesAndObjects.getInfo(name);
	}

}
