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
import progress.message.jclient.MessageConsumer;


public class MessageConsumerInfo extends Info
{
	private MessageConsumer		messageConsumer;
	private SessionInfo			creator;
	private String				createDestination;
	private String				createMessageSelector;
	private Boolean				createNoLocal;
	private String				createdBy;
	private Integer				prefetchThreshold;
	private Integer				prefetchCount;


	public MessageConsumerInfo(
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
							"prefetchCount=int",
							"prefetchThreshold=int",
							"create=scope",
		};
		createScopeSchema = new String[] {
							"destination=string",
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
			createDestination = cfg.lookupString(scope, "create.destination");
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
		prefetchThreshold = lookupOptionalInt("prefetchThreshold");
		prefetchCount = lookupOptionalInt("prefetchCount");

		try {
			config4jms.getDestination(createDestination);
		} catch (Config4JMSException ex) {
			throw new Config4JMSException(cfg.fileName() + ": value of "
					+ cfg.mergeNames(scope, "create.destination") + " ('"
					+ createDestination + "') does not specify the name of a "
					+ "Queue, Topic or MultiTopic.");
		}
	}


	public void createJMSObject() throws Config4JMSException
	{
		Session			session;
		Destination		dest;

		session = creator.getSession();
		try {
			//--------
			// One of the following:
			//    createConsumer(dest)
			//    createConsumer(dest, messageSelector)
			//    createConsumer(dest, messageSelector, noLocal)
			//--------
			dest = config4jms.getDestination(createDestination);
			if (createNoLocal == null) {
				if (createMessageSelector == null) {
					messageConsumer = (MessageConsumer)
						session.createConsumer(dest);
				} else {
					messageConsumer = (MessageConsumer)
						session.createConsumer(dest, createMessageSelector);
				}
			} else {
					messageConsumer = (MessageConsumer)
						session.createConsumer(dest, createMessageSelector,
											   createNoLocal.booleanValue());
			}
		} catch (JMSException ex) {
			throw new Config4JMSException(ex, cfg.fileName() + ": error in "
									+ "creating object for " + scope
									+ "; Session.createConsumer() failed: "
									+ ex.toString());
		}

		setAttribute(messageConsumer, "prefetchCount", "setPrefetchCount",
					 int.class, prefetchCount);
		setAttribute(messageConsumer, "prefetchThreshold",
					 "setPrefetchThreshold", int.class, prefetchThreshold);
	}


	public Object getObject()
	{
		return messageConsumer;
	}

}
