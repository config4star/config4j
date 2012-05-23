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

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TemporaryTopic;

import javax.naming.NamingException;

import org.config4j.Configuration;
import org.config4j.ConfigurationException;
import org.config4j.SchemaValidator;

import org.config4jms.Config4JMSException;

import org.config4jms.base.ObtainMethodData;
import org.config4jms.base.TypeDefinition;


public class TemporaryTopicInfo extends Info
{
	private SessionInfo			parent;
	private String				session;
	private TemporaryTopic		temporaryTopic;


	public TemporaryTopicInfo(
		org.config4jms.Config4JMS	config4jms,
		String						scope,
		TypeDefinition				typeDef) throws Config4JMSException
	{
		super(config4jms, scope, typeDef);
	}


	public void validateConfiguration() throws Config4JMSException
	{
		String[]				scopeSchema;
		String[]				createScopeSchema;
		SchemaValidator			scopeSv = new SchemaValidator();

		scopeSchema = new String[] {"session=string"};

		try {
			//--------
			// Schema checks for scope and scope+".create"
			//--------
			scopeSv.parseSchema(scopeSchema);
			scopeSv.validate(cfg, scope, "", false,
							 Configuration.CFG_SCOPE_AND_VARS);

			//--------
			// Get the required attributes
			//--------
			session = cfg.lookupString(scope, "session");
		} catch(ConfigurationException ex) {
			throw new Config4JMSException(ex.getMessage());
		}

		//--------
		// Check that cross-references to other scopes are valid.
		//--------
		parent = (SessionInfo)jms.getInfo(session);
		if (parent == null) {
			throw new Config4JMSException(cfg.fileName() + ": value of "
							+ cfg.mergeNames(scope, "session") + " ('"
							+ session + "') does not specify the name of a "
							+ "Session.");
		}
	}


	public void createJMSObject() throws Config4JMSException
	{
		ObtainMethodData		omData;
		Session					session;
		Object					obj = null;

		session = parent.getSession();
		try {
			temporaryTopic = session.createTemporaryTopic();
		} catch(JMSException ex) {
			throw new Config4JMSException(ex, cfg.fileName() + ": error in "
								+ "creating object for " + scope
								+ "; Session.createTemporaryTopic() failed: "
								+ ex.toString());
		}
	}


	TemporaryTopic getTemporaryTopic()
	{
		return temporaryTopic;
	}


	public Object getObject()
	{
		return temporaryTopic;
	}

}
