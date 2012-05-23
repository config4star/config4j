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

package org.config4jms.base;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.HashMap;

import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.Session;

import org.config4j.Configuration;
import org.config4j.ConfigurationException;
import org.config4j.EnumNameAndValue;
import org.config4j.ValueWithUnits;

import org.config4jms.Config4JMS;
import org.config4jms.Config4JMSException;


public abstract class Info
{
	protected Config4JMS		config4jms;
	protected Object			obj;
	protected Configuration		cfg;
	protected String			scope;
	protected String			name;
	protected String			type;
	protected TypeDefinition	typeDef;

	public Info(
		Config4JMS				config4jms,
		String					scope,
		TypeDefinition			typeDef) throws Config4JMSException
	{
		String					tmp;

		this.config4jms = config4jms;
		this.cfg = config4jms.getConfiguration();
		this.scope = scope;
		this.typeDef = typeDef;

		//--------
		// scope is of the form: "<prefix>.<type>.<name>"
		// Extract the "<name>" and "<type>" components.
		//--------
		this.name = scope.substring(scope.lastIndexOf('.') + 1);
		tmp = scope.substring(0, scope.lastIndexOf('.'));
		this.type = tmp.substring(tmp.lastIndexOf('.') + 1);
		if (!type.equals(typeDef.getTypeName())) {
			System.err.println("BUG! scope '" + scope + "' is not of type '"
							+ typeDef.getTypeName() + "'");
			System.exit(1);
		}
	}


	public boolean isA(String type)
	{
		return typeDef.isa(type);
	}


	public abstract Object getObject();
	
	public abstract void validateConfiguration() throws Config4JMSException;

	public abstract void createJMSObject() throws Config4JMSException;


	public String getScope()
	{
		return scope;
	}


	public String getName()
	{
		return name;
	}


	public String getType()
	{
		return type;
	}


	public String lookupOptionalString(String localName)
	{
		if (cfg.type(scope, localName) == Configuration.CFG_NO_VALUE) {
			return null;
		}
		return cfg.lookupString(scope, localName);
	}


	public String[] lookupOptionalList(String localName)
	{
		if (cfg.type(scope, localName) == Configuration.CFG_NO_VALUE) {
			return null;
		}
		return cfg.lookupList(scope, localName);
	}


	public Boolean lookupOptionalBool(String localName)
	{
		if (cfg.type(scope, localName) == Configuration.CFG_NO_VALUE) {
			return null;
		}
		return new Boolean(cfg.lookupBoolean(scope, localName));
	}


	public Integer lookupOptionalInt(String localName)
	{
		if (cfg.type(scope, localName) == Configuration.CFG_NO_VALUE) {
			return null;
		}
		return new Integer(cfg.lookupInt(scope, localName));
	}


	public Long lookupOptionalLong(String localName)
	{
		if (cfg.type(scope, localName) == Configuration.CFG_NO_VALUE) {
			return null;
		}
		return new Long(cfg.lookupInt(scope, localName));
	}


	public Float lookupOptionalFloat(String localName)
	{
		if (cfg.type(scope, localName) == Configuration.CFG_NO_VALUE) {
			return null;
		}
		return new Float(cfg.lookupFloat(scope, localName));
	}


	public Integer lookupOptionalDurationMilliseconds(String localName)
	{
		int				result;

		if (cfg.type(scope, localName) == Configuration.CFG_NO_VALUE) {
			return null;
		}
		result = cfg.lookupDurationMilliseconds(scope, localName);
		if (result == -1) { result = 0; }
		return new Integer(result);
	}


	public Integer lookupOptionalDurationSeconds(String localName)
	{
		int				result;

		if (cfg.type(scope, localName) == Configuration.CFG_NO_VALUE) {
			return null;
		}
		result = cfg.lookupDurationSeconds(scope, localName);
		if (result == -1) { result = 0; }
		return new Integer(result);
	}


	public Integer lookupOptionalDurationMinutes(String localName)
	{
		int				result;

		if (cfg.type(scope, localName) == Configuration.CFG_NO_VALUE) {
			return null;
		}
		result = cfg.lookupDurationSeconds(scope, localName);
		if (result == -1) { result = 0; }
		return new Integer(result / 60);
	}


	public Long lookupOptionalDurationMillisecondsLong(String localName)
	{
		int				result;

		if (cfg.type(scope, localName) == Configuration.CFG_NO_VALUE) {
			return null;
		}
		result = cfg.lookupDurationMilliseconds(scope, localName);
		if (result == -1) { result = 0; }
		return new Long(result);
	}


	public Long lookupOptionalDurationSecondsLong(String localName)
	{
		int				result;

		if (cfg.type(scope, localName) == Configuration.CFG_NO_VALUE) {
			return null;
		}
		result = cfg.lookupDurationSeconds(scope, localName);
		if (result == -1) { result = 0; }
		return new Long(result);
	}


	public Integer lookupOptionalJMSDeliveryMode(String localName)
	{
		int				result;

		if (cfg.type(scope, localName) == Configuration.CFG_NO_VALUE) {
			return null;
		}
		result = cfg.lookupEnum(scope, localName, "JMSDeliveryMode",
					new EnumNameAndValue[]{
					new EnumNameAndValue("DEFAULT",
						Message.DEFAULT_DELIVERY_MODE),
					new EnumNameAndValue("NON_PERSISTENT",
						DeliveryMode.NON_PERSISTENT),
					new EnumNameAndValue("PERSISTENT",
						DeliveryMode.PERSISTENT),
					});
		return new Integer(result);
	}


	public int lookupRequiredAcknowledgeMode(String localName)
	{
		return cfg.lookupEnum(scope, localName, "acknowledgeMode",
							new EnumNameAndValue[]{
								new EnumNameAndValue("AUTO_ACKNOWLEDGE",
									Session.AUTO_ACKNOWLEDGE),
								new EnumNameAndValue("CLIENT_ACKNOWLEDGE",
									Session.CLIENT_ACKNOWLEDGE),
								new EnumNameAndValue("DUPS_OK_ACKNOWLEDGE",
									Session.DUPS_OK_ACKNOWLEDGE),
							});
	}


	public Integer lookupOptionalMemorySize(String localName)
	{
		ValueWithUnits		vu;
		int					result;
		String				units;

		if (cfg.type(scope, localName) == Configuration.CFG_NO_VALUE) {
			return null;
		}

		//--------
		// Config4JMS was developed before the lookupMemorySizeBytes()
		// operation was introduced in Config4*. Ideally, the code
		// below should be updated to call cfg.lookupMemorySizeBytes(),
		// but I no longer have a license for a JMS product and I do
		// not want to make code changes without having a way to test it.
		//--------
		vu = cfg.lookupFloatWithUnits(scope, localName, "memorySize",
				new String[]{"bytes", "KB", "MB"});
		units = vu.getUnits();
		if (units.equals("bytes")) {
			result = (int)vu.getFloatValue();
		} else if (units.equals("KB")) {
			result = (int)vu.getFloatValue() * 1024;
		} else { // MB
			result = (int)vu.getFloatValue() * 1024 * 1024;
		}
		return new Integer(result);
	}


	public Long lookupOptionalMemorySizeLong(String localName)
	{
		Integer				result;

		result = lookupOptionalMemorySize(localName);
		if (result == null) {
			return null;
		}
		return new Long(result.intValue());
	}


	public Object importFromFileOrJNDI(
		ObtainMethodData	omData,
		String				scope,
		Class				desiredClass) throws Config4JMSException
	{
		String				obtainMethod;
		String				desiredClassName;
		String				className;
		Object				obj;
		Class[]				objClasses;
		int					i;

		obtainMethod = omData.getMethod() + omData.getArg();
		try {
			if (omData.getMethod().equals("jndi#")) {
				obj = config4jms.importObjectFromJNDI(omData.getArg());
			} else {
				obj = config4jms.importObjectFromFile(omData.getArg());
			}
		} catch(Exception ex) {
			throw new Config4JMSException(cfg.fileName() + ": error in "
				+ "processing " + scope + ".obtainMethod ('"
				+ obtainMethod + "'): " + ex.toString());
		}

		//--------
		// Check for any of the following matches the desired class:
		//   (1) The object's class.
		//   (2) Any of the object's interfaces.
		//   (3) Any of the object's ancestor classes.
		//--------
		if (obj.getClass().equals(desiredClass)) {
			return obj;
		}
		objClasses = obj.getClass().getInterfaces();
		for (i = 0; i < objClasses.length; i++) {
			className = objClasses[i].getName();
			desiredClassName = desiredClass.getName();
			if (objClasses[i].equals(desiredClass)) {
				return obj;
			}
		}
		objClasses = obj.getClass().getClasses();
		for (i = 0; i < objClasses.length; i++) {
			className = objClasses[i].getName();
			desiredClassName = desiredClass.getName();
			if (objClasses[i].equals(desiredClass)) {
				return obj;
			}
		}

		//--------
		// No match, so throw an exception
		//--------
		throw new Config4JMSException(cfg.fileName() + ": error in "
				+ "processing " + scope + ".obtainMethod ('"
				+ obtainMethod + "'); imported object is of type '"
				+ obj.getClass().getName() + "' instead of "
				+ desiredClass.getName());
	}


	public ObtainMethodData parseObtainMethod(String instruction,
						String[] allowedMethods)
	{
		int				i;
		int				len;
		boolean			hasHash;
		String			method;
		String			arg;

		for (i = 0; i < allowedMethods.length; i++) {
			method = allowedMethods[i];
			len = method.length();
			hasHash = method.endsWith("#");
			if (!hasHash) {
				if (instruction.equals(method)) {
					return new ObtainMethodData(method, ""); // success
				} else {
					continue;
				}
			}
			if (!instruction.startsWith(method)) {
					continue;
			}
			arg = instruction.substring(method.length());
			if (arg.length() == 0) {
				return new ObtainMethodData(); // failure: missing arg
			} else {
				return new ObtainMethodData(method, arg); // success
			}
		}
		return new ObtainMethodData(); // failure: unknown method
	}


	protected void setAttribute(
		Object			target,
		String			cfgName,
		String			methodName,
		Class			valueClass,
		Object			value) throws Config4JMSException
	{
		Method			method;
		Object			result;
		String			strValue = null;
		String[]		listValue = null;
		StringBuffer	buf;
		int				i;

		try {
			method = target.getClass().getMethod(methodName,
													new Class[]{valueClass});
			if (value == null) { return; }
			result = method.invoke(target, new Object[]{value});
		} catch(InvocationTargetException ex) {
			try {
				if (value.getClass().equals(String[].class)) {
					buf = new StringBuffer();
					listValue = cfg.lookupList(scope, cfgName);
					for (i = 0; 0 < listValue.length; i++) {
						buf.append("'").append(listValue[i]).append("'");
						if (i < listValue.length-1) {
							buf.append(", ");
						}
					}
					strValue= buf.toString();
				} else {
					strValue = cfg.lookupString(scope, cfgName);
				}
			} catch(ConfigurationException ex2) {
				// Bug!
				ex2.printStackTrace();
				System.exit(1);
			}
			throw new Config4JMSException(cfg.fileName() + ": error when "
					+ "setting attribute for '" + cfg.mergeNames(scope, cfgName)
					+ "' ('" + strValue + "'): "
					+ ex.getTargetException().toString());
		} catch(Exception ex) {
			// Bug!
			ex.printStackTrace();
			throw new Config4JMSException(ex.toString());
		}
	}

}
