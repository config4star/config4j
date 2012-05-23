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

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

import org.config4j.Configuration;
import org.config4j.ConfigurationException;
import org.config4j.SchemaValidator;

import org.config4jms.Config4JMSException;

import org.config4jms.base.TypeDefinition;


public class SessionInfo extends Info
{
	private ConnectionInfo		creator;
	private Session				session;
	private boolean				createTransacted;
	private int					createAcknowledgeMode;
	private String				createdBy;


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
							"createdBy=string",
							"create=scope",
		};
		createScopeSchema = new String[] {
							"@typedef acknowledgeMode=enum["
								+ "AUTO_ACKNOWLEDGE, "
								+ "CLIENT_ACKNOWLEDGE, "
								+ "DUPS_OK_ACKNOWLEDGE]",
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
			createAcknowledgeMode = lookupRequiredAcknowledgeMode(
													"create.acknowledgeMode");
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

		//System.out.println("Created " + scope);
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
