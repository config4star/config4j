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

package org.config4j;


public abstract class SchemaType implements Comparable<SchemaType>
{

	public SchemaType(String typeName, int cfgType)
	{
		this.typeName  = typeName;
		this.className = this.getClass().getName();
		this.cfgType   = cfgType;
	}


	public int compareTo(SchemaType o)
	{
		return typeName.compareTo(o.typeName);
	}


	abstract public void checkRule(
		SchemaValidator		sv,
		Configuration		cfg,
		String				typeName,
		String[]			typeArgs,
		String				rule) throws ConfigurationException;


	public void validate(
		SchemaValidator		sv,
		Configuration		cfg,
		String				scope,
		String				name,
		String				typeName,
		String				origTypeName,
		String[]			typeArgs,
		int					indentLevel) throws ConfigurationException
	{
		String				value;
		StringBuffer		errSuffix;
		String				fullyScopedName;
		String				sep;

		value = cfg.lookupString(scope, name);
		errSuffix = new StringBuffer();

		if (!sv.callIsA(this, cfg, value, typeName, typeArgs, indentLevel+1,
					    errSuffix))
		{
			fullyScopedName = Configuration.mergeNames(scope, name);
			if (errSuffix.length() > 0) {
				sep = "; ";
			} else {
				sep = "";
			}
			throw new ConfigurationException(cfg.fileName() + ": bad "
					+ typeName + " value ('" + value + "') for '"
					+ fullyScopedName + "'" + sep + errSuffix);
		}
	}


	public boolean isA(
		SchemaValidator		sv,
		Configuration		cfg,
		String				value,
		String				typeName,
		String[]			typeArgs,
		int					indentLevel,
		StringBuffer		errSuffix)
	{
		return false;
	}


	public String getTypeName()  { return typeName; }
	public int    getCfgType()   { return cfgType; }
	String        getClassName() { return className; }


	protected SchemaType findType(SchemaValidator sv, String name)
	{
		return sv.findType(name);
	}


	protected final void callValidate(
		SchemaType			target,
		SchemaValidator		sv,
		Configuration		cfg,
		String				scope,
		String				name,
		String				typeName,
		String				origTypeName,
		String[]			typeArgs,
		int					indentLevel) throws ConfigurationException
	{
		sv.callValidate(target, cfg, scope, name, typeName, origTypeName,
						typeArgs, indentLevel);
	}


	protected final boolean callIsA(
		SchemaType			target,
		SchemaValidator		sv,
		Configuration		cfg,
		String				value,
		String				typeName,
		String[]			typeArgs,
		int					indentLevel,
		StringBuffer		errSuffix) throws ConfigurationException
	{
		return sv.callIsA(target, cfg, value, typeName, typeArgs, indentLevel,
						  errSuffix);
	}


	//--------
	// Instance variables
	//--------
	private String			typeName;
	private String			className;
	private int				cfgType;
}
