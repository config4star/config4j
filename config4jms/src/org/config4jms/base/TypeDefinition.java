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
import org.config4j.ConfigurationException;

import org.config4jms.Config4JMS;
import org.config4jms.Config4JMSException;


public class TypeDefinition
{
	private String			typeName;
	private String[]		ancestorTypeNames;
	private boolean			isAbstract;
	private HashMap			infoMap;
	private Constructor		constructor;

	public TypeDefinition(
		String				typeName,
		String[]			ancestorTypeNames,
		String				className)
	{
		Class				c;

		this.typeName  = typeName;
		if (ancestorTypeNames == null) {
			this.ancestorTypeNames = new String[0];
		} else {
			this.ancestorTypeNames = ancestorTypeNames;
		}
		this.isAbstract = (className == null);
		infoMap = new HashMap();

		if (!isAbstract) {
			try {
				c = Class.forName(className);
				constructor = c.getConstructor(new Class[]{
					Config4JMS.class, String.class, TypeDefinition.class});
			} catch(Exception ex) {
				System.err.println("\n\nBug!");
				ex.printStackTrace();
				System.exit(1);
			}
		}
	}


	String      getTypeName()          { return typeName; }
	Constructor getConstructor()       { return constructor; }
	String[]    getAncestorTypeNames() { return ancestorTypeNames; }
	boolean     getIsAbstract()        { return isAbstract; }


	boolean isa(String anotherType)
	{
		int				i;

		if (anotherType.equals(typeName)) {
			return true;
		}
		if (ancestorTypeNames == null) {
			return false;
		}
		for (i = 0; i < ancestorTypeNames.length; i++) {
			if (ancestorTypeNames[i].equals(anotherType)) {
				return true;
			}
		}
		return false;
	}


	Iterator getInfoIterator() {
		return infoMap.values().iterator();
	}


	void add(Info info)
	{
		infoMap.put(info.getName(), info);
	}

}
