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

import org.config4j.Configuration;
import org.config4j.EnumNameAndValue;
import org.config4jms.Config4JMSException;
import org.config4jms.base.TypeDefinition;
import progress.message.jclient.Constants;


abstract class Info extends org.config4jms.base.Info
{
	protected Config4JMS			jms;


	public Info(
		org.config4jms.Config4JMS	config4jms,
		String						scope,
		TypeDefinition				typeDef) throws Config4JMSException
	{
		super(config4jms, scope, typeDef);
		jms = (Config4JMS)config4jms;
	}


	Integer lookupOptionalAsyncDeliveryMode(String localName)
	{
		int				result;

		if (cfg.type(scope, localName) == Configuration.CFG_NO_VALUE) {
			return null;
		}
		result = cfg.lookupEnum(scope, localName, "AsynchronousDeliveryMode",
					new EnumNameAndValue[]{
					new EnumNameAndValue("DEFAULT",
						Constants.ASYNC_DELIVERY_MODE_DEFAULT),
					new EnumNameAndValue("ENABLED",
						Constants.ASYNC_DELIVERY_MODE_ENABLED),
					new EnumNameAndValue("DISABLED",
						Constants.ASYNC_DELIVERY_MODE_DISABLED),
					});
		return new Integer(result);
	}


	Integer lookupOptionalFlowToDisk(String localName)
	{
		int				result;

		if (cfg.type(scope, localName) == Configuration.CFG_NO_VALUE) {
			return null;
		}
		result = cfg.lookupEnum(scope, localName, "FlowToDisk",
					new EnumNameAndValue[]{
					new EnumNameAndValue("OFF",
						Constants.FLOW_TO_DISK_OFF),
					new EnumNameAndValue("ON",
						Constants.FLOW_TO_DISK_ON),
					new EnumNameAndValue("USE_BROKER_SETTING",
						Constants.FLOW_TO_DISK_USE_BROKER_SETTING),
					});
		return new Integer(result);
	}

}
