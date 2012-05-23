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

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.QueueBrowser;
import javax.jms.Session;

import org.config4j.Configuration;
import org.config4j.ConfigurationException;
import org.config4j.SchemaValidator;

import org.config4jms.Config4JMSException;

import org.config4jms.base.ObtainMethodData;
import org.config4jms.base.TypeDefinition;

import progress.message.jclient.ConnectionFactory;


public class ConnectionFactoryInfo extends Info
{
	private ConnectionFactory	connectionFactory;

	private String				createBrokerURL;
	private String				createDefaultUserName;
	private String				createDefaultPassword;
	private String				createConnectID;
	private String				obtainMethod;
	private Integer				asyncDeliveryMode;
	private String				clientID;
	private Long				clientTransactionBufferSize;
	private String				connectionURLs;
	private String				defaultPassword;
	private Integer				defaultTxnBatchSize;
	private String				defaultUser;
	private Long				deliveryCloseTimeout; // milliseconds
	private Integer				deliveryDoubtWindow;
	private Boolean				durableMessageOrder;
	private Boolean				enableActionalInstrumentation;
	private Boolean				enableLocalStore;
	private Boolean				faultTolerant;
	private Integer				faultTolerantReconnectTimeout; // seconds
	private Integer				flowToDisk; // enum
	private Integer				initialConnectTimeout; // seconds
	private Integer				initialRcvBufferSize; // memory size
	private Integer				initialSendBufferSize; // memory size
	private Integer				LGDownStreamNodeType;
	private Boolean				loadBalancing;
	private String				localStoreDirectory;
	private Long				localStoreSize;
	private Integer				localStoreWaitTime; // seconds
	private String				loginSPI;
	private Integer				maxDeliveryCount;
	private Integer				maxRcvBufferSize; // memory size
	private Integer				maxSendBufferSize; // memory size
	private Integer				minRcvBufferSize; // memory size
	private Integer				minSendBufferSize; // memory size
	private Integer				monitorInterval; // seconds
	private Boolean				persistentDelivery;
	private Long				pingInterval; // seconds
	private Integer				prefetchCount;
	private Integer				prefetchThreshold;
	private Integer				qopCacheSize;
	private Integer				reconnectInterval; // seconds
	private Integer				reconnectTimeout; // minutes
	private Boolean				requireActionalJMSNode;
	private Boolean				selectorAtBroker;
	private Boolean				sequential;
	private Integer				socketConnectTimeout; // milliseconds
	private Boolean				splitMultiTopicDelivery;


	public ConnectionFactoryInfo(
		org.config4jms.Config4JMS	config4jms,
		String						scope,
		TypeDefinition				typeDef) throws Config4JMSException
	{
		super(config4jms, scope, typeDef);
	}


	public void validateConfiguration() throws Config4JMSException
	{
		String[]				scopeSchema;
		SchemaValidator			scopeSv = new SchemaValidator();
		String[]				createScopeSchema;
		SchemaValidator			createScopeSv = new SchemaValidator();
		int						i;
		ConnectionInfo			obj;

		scopeSchema = new String[] {
							"@typedef asynchronousDeliveryMode=enum["
								+ "DISABLED, "
								+ "ENABLED, "
								+ "DEFAULT]",
							"@typedef flowToDisk=enum[OFF, ON, "
								+ "USE_BROKER_SETTING]",
							"@typedef memorySize=float_with_units[bytes, "
								+ "KB, MB, GB]",
							"obtainMethod=string",
							"asynchronousDeliveryMode=asynchronousDeliveryMode",
							"clientID=string",
							"clientTransactionBufferSize=int",
							"connectionURLs=string",
							"defaultPassword=string",
							"defaultTxnBatchSize=memorySize",
							"defaultUser=string",

							"deliveryCloseTimeout=durationMilliseconds",
							"deliveryDoubtWindow=int",
							"durableMessageOrder=boolean",
							"enableQActionalInstrumentation=boolean",
							"enableLocalStore=boolean",
							"faultTolerant=boolean",
							"faultTolerantReconnectTimeout=durationSeconds",
							"flowToDisk=flowToDisk",
							"initialConnectTimeout=durationSeconds",
							"initialRcvBufferSize=memorySize",
							"initialSendBufferSize=memorySize",
							"LGDownStreamNodeType=int",
							"loadBalancing=boolean",
							"localStoreDirectory=string",
							"localStoreSize=int",
							"localStoreWaitTime=durationSeconds",
							"loginSPI=string",
							"maxDeliveryCount=int",
							"maxRcvBufferSize=memorySize",
							"maxSendBufferSize=memorySize",
							"minRcvBufferSize=memorySize",
							"minSendBufferSize=memorySize",
							"monitorInterval=durationSeconds",
							"persistentDelivery=boolean",
							"pingInterval=durationSeconds",
							"prefetchCount=int",
							"prefetchThreshold=int",
							"qopCacheSize=int",
							"reconnectInternal=durationSeconds",
							"reconnectTimeout=durationSeconds",
							"requireActionalJMSNode=boolean",
							"selectorAtBroker=boolean",
							"sequential=boolean",
							"socketConnectTimeout=durationMilliseconds",
							"splitMultiTopicDelivery=boolean",

							"create = scope",
		};
		createScopeSchema = new String[] {
							"brokerURL=string",
							"defaultUserName=string",
							"defaultPassword=string",
							"connectID=string",
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
			obtainMethod = cfg.lookupString(scope, "obtainMethod");
		} catch(ConfigurationException ex) {
			throw new Config4JMSException(ex.getMessage());
		}

		//--------
		// Get the optional attributes
		//--------
		createBrokerURL = lookupOptionalString("create.brokerURL");
		createDefaultUserName = lookupOptionalString("create.defaultUserName");
		createDefaultPassword = lookupOptionalString("create.defaultPassword");
		createConnectID = lookupOptionalString("create.connectID");

		asyncDeliveryMode = lookupOptionalAsyncDeliveryMode(
										"asynchronousDeliveryMode");
		clientID = lookupOptionalString("clientID");
		clientTransactionBufferSize = lookupOptionalMemorySizeLong(
										"clientTransactionBufferSize ");
		connectionURLs = lookupOptionalString("connectionURLs");
		defaultPassword = lookupOptionalString("defaultPassword");
		defaultTxnBatchSize = lookupOptionalMemorySize("defaultTxnBatchSize");
		defaultUser = lookupOptionalString("defaultUser");
		deliveryCloseTimeout = lookupOptionalDurationMillisecondsLong(
											"deliveryCloseTimeout");
		deliveryDoubtWindow = lookupOptionalInt("deliveryDoubtWindow");
		durableMessageOrder = lookupOptionalBool("durableMessageOrder");
		enableActionalInstrumentation = lookupOptionalBool(
										"enableActionalInstrumentation");
		enableLocalStore = lookupOptionalBool("enableLocalStore");
		faultTolerant = lookupOptionalBool("faultTolerant");
		faultTolerantReconnectTimeout = lookupOptionalDurationSeconds(
										"faultTolerantReconnectTimeout");
		flowToDisk = lookupOptionalFlowToDisk("flowToDisk");
		initialConnectTimeout = lookupOptionalDurationSeconds(
										"initialConnectTimeout");
		initialRcvBufferSize = lookupOptionalMemorySize("initialRcvBufferSize");
		initialSendBufferSize = lookupOptionalMemorySize(
												"initialSendBufferSize");
		LGDownStreamNodeType = lookupOptionalInt("LGDownStreamNodeType");
		loadBalancing = lookupOptionalBool("loadBalancing");
		localStoreDirectory = lookupOptionalString("localStoreDirectory");
		localStoreSize = lookupOptionalLong("localStoreSize");
		localStoreWaitTime = lookupOptionalDurationSeconds(
													"localStoreWaitTime");
		loginSPI = lookupOptionalString("loginSPI");
		maxDeliveryCount = lookupOptionalInt("maxDeliveryCount");
		maxRcvBufferSize = lookupOptionalMemorySize("maxRcvBufferSize");
		maxSendBufferSize = lookupOptionalMemorySize("maxSendBufferSize");
		minRcvBufferSize = lookupOptionalMemorySize("minRcvBufferSize");
		minSendBufferSize = lookupOptionalMemorySize("minSendBufferSize");
		monitorInterval = lookupOptionalDurationSeconds("monitorInterval");
		persistentDelivery = lookupOptionalBool("persistentDelivery");
		pingInterval = lookupOptionalDurationSecondsLong("pingInterval");
		prefetchCount = lookupOptionalInt("prefetchCount");
		prefetchThreshold = lookupOptionalInt("prefetchThreshold");
		qopCacheSize = lookupOptionalInt("qopCacheSize");;
		reconnectInterval = lookupOptionalDurationSeconds("reconnectInterval");
		reconnectTimeout = lookupOptionalDurationMinutes("reconnectTimeout");
		requireActionalJMSNode = lookupOptionalBool("requireActionalJMSNode");
		selectorAtBroker = lookupOptionalBool("selectorAtBroker");
		sequential = lookupOptionalBool("sequential");
		socketConnectTimeout = lookupOptionalDurationMilliseconds(
										"socketConnectTimeout");
		splitMultiTopicDelivery = lookupOptionalBool("splitMultiTopicDelivery");

	}


	public void createJMSObject() throws Config4JMSException
	{
		ObtainMethodData		omData;
		Object					obj;

		omData = parseObtainMethod(obtainMethod,
								   new String[]{"jndi#", "file#", "create"});
		if (!omData.isOK()) {
			throw new Config4JMSException(cfg.fileName() + ": bad value ('"
					+ obtainMethod + "') for '"
					+ cfg.mergeNames(scope, "obtainMethod")
					+ "'; should be 'jndi#path', 'file#filename' or 'create'");
		}
		if (omData.getMethod().equals("create")) {
			createConnectionFactory();
		} else {
			connectionFactory = (ConnectionFactory)importFromFileOrJNDI(
									omData, scope, ConnectionFactory.class);
		}
		obj = connectionFactory;

		//--------
		// Set optional attributes
		//--------
		setAttribute(obj, "asynchronousDeliveryMode",
				"setAsynchronousDeliveryMode",
				Integer.class, asyncDeliveryMode);
		setAttribute(obj, "clientTransactionBufferSize",
				"setClientTransactionBufferSize",
				Long.class, clientTransactionBufferSize);
		setAttribute(obj, "clientID", "setClientID", String.class, clientID);
		setAttribute(obj, "connectionURLs", "setConnectionURLs",
				String.class, connectionURLs);
		setAttribute(obj, "defaultPassword", "setDefaultPassword",
				String.class, defaultPassword);
		setAttribute(obj, "defaultTxnBatchSize", "setDefaultTxnBatchSize",
				int.class, defaultTxnBatchSize);
		setAttribute(obj, "defaultUser", "setDefaultUser",
				String.class, defaultUser);
		setAttribute(obj, "deliveryCloseTimeout", "setDeliveryCloseTimeout",
				Long.class, deliveryCloseTimeout);
		setAttribute(obj, "deliveryDoubtWindow", "setDeliveryDoubtWindow",
				Integer.class, deliveryDoubtWindow);
		setAttribute(obj, "enableActionalInstrumentation",
				"setEnableActionalInstrumentation",
				boolean.class, enableActionalInstrumentation);
		setAttribute(obj, "enableLocalStore", "setEnableLocalStore",
				boolean.class, enableLocalStore);
		setAttribute(obj, "faultTolerant", "setFaultTolerant",
				Boolean.class, faultTolerant);
		setAttribute(obj, "faultTolerantReconnectTimeout",
				"setFaultTolerantReconnectTimeout",
				Integer.class, faultTolerantReconnectTimeout);
		setAttribute(obj, "flowToDisk", "setFlowToDisk",
				Integer.class, flowToDisk);
		setAttribute(obj, "initialConnectTimeout", "setInitialConnectTimeout",
				Integer.class, initialConnectTimeout);
		setAttribute(obj, "initialRcvBufferSize", "setInitialRcvBufferSize",
				Integer.class, initialRcvBufferSize);
		setAttribute(obj, "initialSendBufferSize", "setInitialSendBufferSize",
				Integer.class, initialSendBufferSize);
		setAttribute(obj, "LGDownStreamNodeType", "setLGDownStreamNodeType",
				int.class, LGDownStreamNodeType);
		setAttribute(obj, "loadBalancing", "setLoadBalancing",
				boolean.class, loadBalancing);
		setAttribute(obj, "localStoreDirectory", "setLocalStoreDirectory",
				String.class, localStoreDirectory);
		setAttribute(obj, "localStoreSize", "setLocalStoreSize",
				long.class, localStoreSize);
		setAttribute(obj, "localStoreWaitTime", "setLocalStoreWaitTime",
				Integer.class, localStoreWaitTime);
		setAttribute(obj, "loginSPI", "setLoginSPI", String.class, loginSPI);
		setAttribute(obj, "maxDeliveryCount", "setMaxDeliveryCount",
				Integer.class, maxDeliveryCount);
		setAttribute(obj, "maxRcvBufferSize", "setMaxRcvBufferSize",
				Integer.class, maxRcvBufferSize);
		setAttribute(obj, "maxSendBufferSize", "setMaxSendBufferSize",
				Integer.class, maxSendBufferSize);
		setAttribute(obj, "minRcvBufferSize", "setMinRcvBufferSize",
				Integer.class, minRcvBufferSize);
		setAttribute(obj, "minSendBufferSize", "setMinSendBufferSize",
				Integer.class, minSendBufferSize);
		setAttribute(obj, "monitorInterval", "setMonitorInterval",
				Integer.class, monitorInterval);
		setAttribute(obj, "persistentDelivery", "setPersistentDelivery",
				boolean.class, persistentDelivery);
		setAttribute(obj, "pingInterval", "setPingIntervalLong",
				Long.class, pingInterval);
		setAttribute(obj, "prefetchCount", "setPrefetchCount",
				int.class, prefetchCount);
		setAttribute(obj, "prefetchThreshold", "setPrefetchThreshold",
				int.class, prefetchThreshold);
		setAttribute(obj, "qopCacheSize", "setQopCacheSize",
				Integer.class, qopCacheSize);
		setAttribute(obj, "reconnectInterval", "setReconnectInterval",
				int.class, reconnectInterval);
		setAttribute(obj, "reconnectTimeout", "setReconnectTimeout",
				int.class, reconnectTimeout);
		setAttribute(obj, "requireActionalJMSNode", "setRequireActionalJMSNode",
				boolean.class, requireActionalJMSNode);
		setAttribute(obj, "selectorAtBroker", "setSelectorAtBroker",
				Boolean.class, selectorAtBroker);
		setAttribute(obj, "sequential", "setSequential",
				boolean.class, sequential);
		setAttribute(obj, "socketConnectTimeout", "setSocketConnectTimeout",
				Integer.class, socketConnectTimeout);
		setAttribute(obj, "splitMultiTopicDelivery",
				"setSplitMultiTopicDelivery",
				Boolean.class, splitMultiTopicDelivery);
	}


	private void createConnectionFactory() throws Config4JMSException
	{
		//--------
		// The ConnectionFactory constructor is overloaded, so we have
		// to laboriously check which parameters have been provided to
		// determine which version of the overloaded constructor we
		// should use.
		//--------
		try {
			if (createBrokerURL == null) {
				connectionFactory = new ConnectionFactory();
				return;
			}
			if (   createDefaultUserName == null
				|| createDefaultPassword == null)
			{
				if (createConnectID == null) {
					connectionFactory = new ConnectionFactory(createBrokerURL);
				} else {
					connectionFactory = new ConnectionFactory(createBrokerURL,
													createConnectID);
				}
				return;
			}
			if (createConnectID == null) {
					connectionFactory = new ConnectionFactory(createBrokerURL,
							createDefaultUserName, createDefaultPassword);
			} else {
					connectionFactory = new ConnectionFactory(createBrokerURL,
							createConnectID, createDefaultUserName,
							createDefaultPassword);
			}
		} catch (JMSException ex) {
			throw new Config4JMSException(ex, cfg.fileName() + ": error in "
								+ "creating object for " + scope
								+ "; ConnectionFactory constructor failed: "
								+ ex.toString());
		}
	}


	ConnectionFactory getConnectionFactory()
	{
		return connectionFactory;
	}


	public Object getObject()
	{
		return connectionFactory;
	}

}
