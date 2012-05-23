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

import javax.jms.Connection;
import javax.jms.JMSException;

import org.config4j.Configuration;
import org.config4j.ConfigurationException;
import org.config4j.EnumNameAndValue;
import org.config4j.SchemaValidator;

import org.config4jms.Config4JMSException;

import org.config4jms.base.TypeDefinition;

import progress.message.jclient.Session;


public class SessionInfo extends Info
{
	private ConnectionInfo		creator;
	private Session				session;
	private boolean				createTransacted;
	private int					createAcknowledgeMode;
	private String				createdBy;
	private Boolean				ackBatchingEnabled;
	private Boolean				durableMessageOrder;
	private Boolean				flowControlDisabled;
	private Integer				flowToDisk;
	private Boolean				splitMultiTopicDelivery;
	private Integer				txnBatchSize; // memory size

	public SessionInfo(
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
							"@typedef flowToDisk=enum[OFF, ON, "
								+ "USE_BROKER_SETTING]",
							"@typedef memorySize=float_with_units[bytes, "
								+ "KB, MB, GB]",
							"createdBy=string",
							"ackBatchingEnabled=boolean",
							"durableMessageOrder=boolean",
							"flowControlDisabled=boolean",
							"flowToDisk=flowToDisk",
							"splitMultiTopicDelivery=boolean",
							"txnBatchSize=memorySize",
							"create=scope",
		};
		createScopeSchema = new String[] {
							"@typedef acknowledgeMode=enum["
								+ "AUTO_ACKNOWLEDGE, "
								+ "CLIENT_ACKNOWLEDGE, "
								+ "DUPS_OK_ACKNOWLEDGE, "
								+ "SINGLE_MESSAGE_ACKNODLEDGE]",
							"transacted=boolean",
							"acknowledgeMode=acknowledgeMode",
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
			createTransacted = cfg.lookupBoolean(scope, "create.transacted");
			createAcknowledgeMode = cfg.lookupEnum(scope,
						"create.acknowledgeMode", "acknowledgeMode",
						new EnumNameAndValue[]{
							new EnumNameAndValue("AUTO_ACKNOWLEDGE",
								Session.AUTO_ACKNOWLEDGE),
							new EnumNameAndValue("CLIENT_ACKNOWLEDGE",
								Session.CLIENT_ACKNOWLEDGE),
							new EnumNameAndValue("DUPS_OK_ACKNOWLEDGE",
								Session.DUPS_OK_ACKNOWLEDGE),
							new EnumNameAndValue("SINGLE_MESSAGE_ACKNOWLEDGE",
								Session.SINGLE_MESSAGE_ACKNOWLEDGE),
						});
		} catch(ConfigurationException ex) {
			throw new Config4JMSException(ex.getMessage());
		}

		creator = (ConnectionInfo)jms.getInfo(createdBy);
		if (creator == null) {
			throw new Config4JMSException(cfg.fileName() + ": value of "
							+ cfg.mergeNames(scope, "createdBy") + " ('"
							+ createdBy + "') does not specify the name of a "
							+ "Connection.");
		}

		//--------
		// Get the optional attributes
		//--------
		ackBatchingEnabled = lookupOptionalBool("ackBatchingEnabled");
		durableMessageOrder = lookupOptionalBool("durableMessageOrder");
		flowControlDisabled = lookupOptionalBool("flowControlDisabled");
		flowToDisk = lookupOptionalFlowToDisk("flowToDisk");
		splitMultiTopicDelivery = lookupOptionalBool("splitMultiTopicDelivery");
		txnBatchSize = lookupOptionalMemorySize("txnBatchSize");
	}


	public void createJMSObject() throws Config4JMSException
	{
		Connection			connection;

		connection = creator.getConnection();
		try {
			session = (Session)connection.createSession(createTransacted,
										createAcknowledgeMode);
		} catch (JMSException ex) {
			throw new Config4JMSException(ex, cfg.fileName() + ": error in "
					+ "creating object for " + scope
					+ "); Connection.createSession() failed: "
					+ ex.toString());
		}

		setAttribute(session, "ackBatchingEnabled", "setAckBatchingEnabled",
					 boolean.class, ackBatchingEnabled);
		setAttribute(session, "durableMessageOrder", "setDurableMessageOrder",
					 boolean.class, durableMessageOrder);
		setAttribute(session, "flowControlDisabled", "setFlowControlDisabled",
					 boolean.class, flowControlDisabled);
		setAttribute(session, "flowToDisk", "setFlowToDisk", int.class,
					 flowToDisk);
		setAttribute(session, "splitMultiTopicDelivery",
					 "setSplitMultiTopicDelivery", boolean.class,
					 splitMultiTopicDelivery);
		setAttribute(session, "txnBatchSize", "setTxnBatchSize", int.class,
					 txnBatchSize);
	}


	Session getSession()
	{
		return session;
	}


	public Object getObject()
	{
		return session;
	}

}
