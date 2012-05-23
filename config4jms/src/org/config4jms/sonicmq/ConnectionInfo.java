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

import javax.jms.JMSException;

import org.config4j.Configuration;
import org.config4j.ConfigurationException;
import org.config4j.SchemaValidator;

import org.config4jms.Config4JMSException;

import org.config4jms.base.TypeDefinition;

import progress.message.jclient.Connection;
import progress.message.jclient.ConnectionFactory;


public class ConnectionInfo extends Info
{
	private ConnectionFactoryInfo	creator;
	private Connection				connection;
	private String					createUserName;
	private String					createPassword;
	private String					createdBy;
	private String					clientID;
	private Long					pingInterval; // seconds


	public ConnectionInfo(
		org.config4jms.Config4JMS	config4jms,
		String						scope,
		TypeDefinition				typeDef) throws Config4JMSException
	{
		super(config4jms, scope, typeDef);
	}


	public void validateConfiguration() throws Config4JMSException
	{
		String[]					scopeSchema;
		SchemaValidator				scopeSv = new SchemaValidator();
		String[]					createScopeSchema;
		SchemaValidator				createScopeSv = new SchemaValidator();
		int							i;

		scopeSchema = new String[] {
							"createdBy=string",
							"clientID=string",
							"pingInterval=durationSeconds",
							"create=scope",
		};
		createScopeSchema = new String[] {
							"userName=string",
							"password=string",
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
		} catch(ConfigurationException ex) {
			throw new Config4JMSException(ex.getMessage());
		}

		//--------
		// Check that cross-references to other scopes are valid.
		//--------
		creator = (ConnectionFactoryInfo)jms.getInfo(createdBy);
		if (creator == null) {
			throw new Config4JMSException(cfg.fileName() + ": value of "
							+ cfg.mergeNames(scope, "createdBy") + " ('"
							+ createdBy + "') does not specify the name of a "
							+ "ConnectionFactory.");
		}

		//--------
		// Get the optional attributes
		//--------
		createUserName = lookupOptionalString("create.userName");
		createPassword = lookupOptionalString("create.password");
		if ((createUserName == null) ^ (createPassword == null)) {
			throw new Config4JMSException(cfg.fileName() + ": in scope "
					+ scope + ", you must specify values for both 'userName' "
					+ "and 'password', or for neither of them.");
		}
		clientID       = lookupOptionalString("clientID");
		pingInterval   = lookupOptionalDurationSecondsLong("pingInterval");
	}


	public void createJMSObject() throws Config4JMSException
	{
		ConnectionFactory			factory;

		factory = creator.getConnectionFactory();
		try {
			if (createUserName == null || createPassword == null) {
				connection = (Connection)factory.createConnection();
			} else {
				connection = (Connection)factory.createConnection(
											createUserName, createPassword);
			}
		} catch (JMSException ex) {
			throw new Config4JMSException(ex, cfg.fileName() + ": error in "
					+ "creating object for " + scope
					+ "; ConnectionFactory.createConnection() failed: "
					+ ex.toString());
		}

		setAttribute(connection, "clientID", "setClientID",
				String.class, clientID);
		setAttribute(connection, "pingInterval", "setPingInterval",
				long.class, pingInterval);
		//System.out.println("Created " + scope);
	}


	Connection getConnection()
	{
		return connection;
	}


	public Object getObject()
	{
		return connection;
	}

}
