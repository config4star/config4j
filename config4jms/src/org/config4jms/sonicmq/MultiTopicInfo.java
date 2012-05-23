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

import javax.jms.Destination;
import javax.jms.JMSException;

import org.config4j.Configuration;
import org.config4j.ConfigurationException;
import org.config4j.SchemaValidator;

import org.config4jms.Config4JMSException;

import org.config4jms.base.TypeDefinition;

import progress.message.jclient.MultiTopic;
import progress.message.jclient.Session;


public class MultiTopicInfo extends Info
{
	private SessionInfo			parent;
	MultiTopic					multiTopic;

	private String				session;
	private String[]			createTopicList;

	public MultiTopicInfo(
		org.config4jms.Config4JMS	config4jms,
		String						scope,
		TypeDefinition				typeDef) throws Config4JMSException
	{
		super(config4jms, scope, typeDef);
	}


	public void validateConfiguration() throws Config4JMSException
	{
		String[]				scopeSchema;
		SchemaValidator			createScopeSv = new SchemaValidator();
		String[]				createScopeSchema;
		SchemaValidator			scopeSv = new SchemaValidator();

		scopeSchema = new String[] {
							"session=string",
							"create=scope",
		};
		createScopeSchema = new String[] {
							"topicList=list[string]",
		};

		try {
			//--------
			// Schema checks for scope and scope+".create"
			//--------
			scopeSv.parseSchema(scopeSchema);
			scopeSv.validate(cfg, scope, "", false,
							 Configuration.CFG_SCOPE_AND_VARS);

			if (cfg.type(scope, "create") == Configuration.CFG_SCOPE) {
				createScopeSv.parseSchema(createScopeSchema);
				createScopeSv.validate(cfg, scope, "create", false,
									   Configuration.CFG_SCOPE_AND_VARS);
			}

			//--------
			// Get the required attributes
			//--------
			session = cfg.lookupString(scope, "session");
			createTopicList = cfg.lookupList(scope, "create.topicList");
		} catch(ConfigurationException ex) {
			throw new Config4JMSException(ex.getMessage());
		}

		//--------
		// Check that cross-references to other scopes are valid.
		//--------
		if (session != null) {
			parent = (SessionInfo)jms.getInfo(session);
			if (parent == null) {
				throw new Config4JMSException(cfg.fileName() + ": value of "
							+ cfg.mergeNames(scope, "session") + " ('"
							+ session + "') does not specify the name of a "
							+ "Session.");
			}
		}
	}


	public void createJMSObject() throws Config4JMSException
	{
		Session			session;
		Destination		dest;
		String			topicName;
		int				i;

		session = parent.getSession();
		try {
			multiTopic = session.createMultiTopic();
		} catch (JMSException ex) {
			throw new Config4JMSException(ex, cfg.fileName() + ": error in "
					+ "creating object for " + scope
					+ "; Session.createMultiTopic() failed: "
					+ ex.toString());
		}

		for (i = 0; i < createTopicList.length; i++) {
			topicName = createTopicList[i];
			try {

				multiTopic.add(config4jms.getDestination(topicName));
			} catch (JMSException ex) {
				throw new Config4JMSException(ex, cfg.fileName() + ": error in "
						+ "creating object for " + scope
						+ "; MultiTopic.add('" + topicName + "')) failed: "
						+ ex.toString());
			}
		}
	}


	public SessionInfo getParent()
	{
		return parent;
	}


	MultiTopic getMultiTopic()
	{
		return multiTopic;
	}


	public Object getObject()
	{
		return multiTopic;
	}

}
