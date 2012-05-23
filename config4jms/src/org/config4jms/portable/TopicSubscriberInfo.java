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
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javax.jms.Session;

import org.config4j.Configuration;
import org.config4j.ConfigurationException;
import org.config4j.SchemaValidator;

import org.config4jms.Config4JMSException;

import org.config4jms.base.TypeDefinition;


public class TopicSubscriberInfo extends Info
{
	private TopicSubscriber		topicSubscriber;
	private SessionInfo			creator;
	private String				createdBy;
	private String				createTopic;
	private String				createMessageSelector;
	private Boolean				createNoLocal;
	private String				createName;

	public TopicSubscriberInfo(
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
		int						i;

		scopeSchema = new String[] {
							"createdBy=string",
							"create=scope",
		};
		createScopeSchema = new String[] {
							"topic=string",
							"messageSelector=string",
							"noLocal=boolean",
							"name=string",
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
			createdBy = cfg.lookupString(scope, "createdBy");
			createTopic = cfg.lookupString(scope, "create.topic");
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
		createMessageSelector = lookupOptionalString("create.messageSelector");
		createNoLocal = lookupOptionalBool("create.noLocal");
		createName = lookupOptionalString("create.name");

		try {
			jms.getInfo(createTopic);
		} catch (Config4JMSException ex) {
			throw new Config4JMSException(cfg.fileName() + ": value of "
					+ cfg.mergeNames(scope, "create.topic") + " ('"
					+ createTopic + "') does not specify the name of a "
					+ "Topic or MultiTopic.");
		}
	}


	public void createJMSObject() throws Config4JMSException
	{
		Session			session;
		Topic			topic;

		session = creator.getSession();
		try {
			//--------
			// One of the following:
			//    createDurableSubscriber(topic, name)
			//    createDurableSubscriber(topic, name, messageSelector,
			//                            noLocal)
			//--------
			topic = jms.getTopic(createTopic);
			if (createNoLocal == null) {
				topicSubscriber = session.createDurableSubscriber(topic,
																createName);
			} else {
				topicSubscriber = session.createDurableSubscriber(topic,
											createName, createMessageSelector,
											createNoLocal.booleanValue());
			}
		} catch (JMSException ex) {
			throw new Config4JMSException(ex, cfg.fileName() + ": error in "
					+ "creating object for " + scope
					+ "; Session.createDurableSubscriber() failed: "
					+ ex.toString());
		}
	}


	TopicSubscriber getTopicSubscriber()
	{
		return topicSubscriber;
	}


	public Object getObject()
	{
		return topicSubscriber;
	}

}
