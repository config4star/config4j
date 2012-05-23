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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.config4j.Configuration;
import org.config4j.SchemaValidator;
import org.config4j.ConfigurationException;

import org.config4jms.Config4JMS;
import org.config4jms.Config4JMSException;


public class TypesAndObjects
{
	private TypeDefinition[]	typeDefinitions;
	private HashMap				objects;


	public TypesAndObjects(TypeDefinition[] typeDefinitions)
	{
		this.typeDefinitions = typeDefinitions;
		objects = new HashMap();
	}


	public boolean isTypeRegistered(String type)
	{
		int						i;
		TypeDefinition			typeDef;

		for (i = 0; i < typeDefinitions.length; i++) {
			typeDef = typeDefinitions[i];
			if (type.equals(typeDef.getTypeName())) {
				return true;
			}
		}
		return false;
	}


	public Info getInfo(String name)
	{
		return (Info)objects.get(name);
	}


	public Info getInfo(String type, String name)
	{
		return (Info)objects.get(type + "," + name);
	}


	public String[] listNamesForType(String type)
	{
		int						i;
		TypeDefinition			typeDef;
		Iterator				iter;
		Info					info;
		ArrayList				result = new ArrayList();

		for (i = 0; i < typeDefinitions.length; i++) {
			typeDef = typeDefinitions[i];
			if (!type.equals(typeDef.getTypeName())) { continue; }
			iter = typeDef.getInfoIterator();
			while (iter.hasNext()) {
				info = (Info)iter.next();
				result.add(info.getName());
			}
		}
		return (String[])result.toArray(new String[0]);
	}


	public void validateConfiguration(Config4JMS config4jms, String scope)
													throws Config4JMSException
	{
		int						i;
		int						j;
		int						k;
		Configuration			cfg;
		ArrayList				schemaList;
		Constructor				constructor;
		TypeDefinition			typeDef;
		SchemaValidator			sv = new SchemaValidator();
		String[]				schema;
		String					typeName;
		String					objName;
		String[]				scopes;
		String[]				ancestorTypeNames;
		String					ancestorTypeName;
		Info					infoObj;
		Info					existingObj;

		cfg = config4jms.getConfiguration();

		//--------
		// Schema check on the top-level scope. It can contain only the
		// non-abstract types specified in typeDefinitions[] plus the
		// following variables:
		//    config4jmsClass
		//    jndiEnvironment
		//--------
		try {
			schemaList = new ArrayList();
			schemaList.add("config4jmsClass=string");
			schemaList.add("jndiEnvironment=table[string,name, string,value]");
			for (i = 0; i < typeDefinitions.length; i++) {
				typeDef = typeDefinitions[i];
				if (typeDef.getIsAbstract()) {
					continue;
				}
				schemaList.add(typeDef.getTypeName() + "=scope");
			}
			schema = (String[])schemaList.toArray(new String[0]);
			sv.parseSchema(schema);
			sv.validate(cfg, scope,"", false, Configuration.CFG_SCOPE_AND_VARS);

			//--------
			// Schema check on each subscope. They should not contain any
			// variables (but can contain sub-scopes with arbitrary names).
			//--------
			schema = new String[0];
			sv = new SchemaValidator();
			sv.parseSchema(schema);
			for (i = 0; i < typeDefinitions.length; i++) {
				typeDef = typeDefinitions[i];
				if (typeDef.getIsAbstract()) { continue; }
				typeName = typeDef.getTypeName();
				if (cfg.type(scope, typeName) == Configuration.CFG_NO_VALUE) {
					continue;
				}
				sv.validate(cfg, scope, typeName, false,
							Configuration.CFG_VARIABLES);
			}
		} catch(ConfigurationException ex) {
			throw new Config4JMSException(ex.getMessage());
		}

		try {
			for (i = 0; i < typeDefinitions.length; i++) {
				typeDef = typeDefinitions[i];
				if (typeDef.getIsAbstract()) { continue; }
				typeName = typeDef.getTypeName();
				if (cfg.type(scope, typeName) == Configuration.CFG_NO_VALUE) {
					continue;
				}
				scopes = cfg.listFullyScopedNames(scope, typeName,
							Configuration.CFG_SCOPE, false);
				//--------
				// Loop over each <type>.<name> subscope, creating a
				// <type>Info object called <name>.
				//--------
				constructor = typeDef.getConstructor();
				for (j = 0; j < scopes.length; j++) {
					infoObj = (Info)constructor.newInstance(
								new Object[]{config4jms, scopes[j], typeDef});
					objName = infoObj.getName();
					existingObj = (Info)objects.get(objName);
					if (existingObj != null) {
						throw new Config4JMSException(cfg.fileName()
								+ ": name clash: you cannot use '"
								+ objName + "' as the name of both a "
								+ infoObj.getType() + " and a "
								+ existingObj.getType());
					}
					//--------
					// Register the Info object in objectsMap under the
					// following names: "<name>", "<type>,<name>" and
					// "<ancestorType>,<name>".
					//--------
					typeDef.add(infoObj);
					objects.put(objName, infoObj);
					objects.put(typeName + "," + objName, infoObj);
					ancestorTypeNames = typeDef.getAncestorTypeNames();
					for (k = 0; k < ancestorTypeNames.length; k++) {
						ancestorTypeName = ancestorTypeNames[k];
						objects.put(ancestorTypeName + "," + objName, infoObj);
					}

					//--------
					// call validateConfiguration() on the newly-created Info.
					//--------
					infoObj.validateConfiguration();
				}
			}
		} catch(Config4JMSException ex) {
			throw ex;
		} catch(ConfigurationException ex) {
			throw new Config4JMSException(ex.getMessage());
		} catch(InvocationTargetException ex) {
			throw (Config4JMSException)ex.getTargetException();
		} catch(Exception ex) {
			//--------
			// Bug!
			//--------
			ex.printStackTrace();
			System.exit(1);
		}
	}


	public void createJMSObjects() throws Config4JMSException
	{
		int						i;
		TypeDefinition			typeDef;
		Info					info;
		Iterator				iter;

		for (i = 0; i < typeDefinitions.length; i++) {
				typeDef  = typeDefinitions[i];
				iter = typeDef.getInfoIterator();
				while (iter.hasNext()) {
					info = (Info)iter.next();
					info.createJMSObject();
				}
		}
	}
}
