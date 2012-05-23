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
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;

import org.config4j.Configuration;
import org.config4j.ConfigurationException;
import org.config4j.SchemaValidator;

import org.config4jms.Config4JMSException;

import org.config4jms.base.TypeDefinition;


public class QueueBrowserInfo extends Info
{
	private QueueBrowser		queueBrowser;
	private SessionInfo			creator;
	private String				createQueue;
	private String				createMessageSelector;
	private String				createdBy;


	public QueueBrowserInfo(
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
							"queue=string",
							"messageSelector=string",
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
			createQueue = cfg.lookupString(scope, "create.queue");
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
	}


	public void createJMSObject() throws Config4JMSException
	{
		Queue			queue;
		Session			session;

		session = creator.getSession();
		try {
			//--------
			// One of the following:
			//    createBrowser(queue)
			//    createBrowser(queue, messageSelector)
			//--------
			queue = config4jms.getQueue(createQueue);
			if (createMessageSelector == null) {
					queueBrowser = (QueueBrowser)session.createBrowser(queue);
			} else {
					queueBrowser = (QueueBrowser)
							session.createBrowser(queue, createMessageSelector);
			}
		} catch (JMSException ex) {
			throw new Config4JMSException(ex, cfg.fileName() + ": error in "
										+ "creating object for " + scope
										+ "; Session.createConsumer() failed: "
										+ ex.toString());
		}
	}


	public QueueBrowser		getQueueBrowser()
	{
		return queueBrowser;
	}


	public Object getObject()
	{
		return queueBrowser;
	}

}
