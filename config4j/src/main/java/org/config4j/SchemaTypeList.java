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

class SchemaTypeList extends SchemaType {

	public SchemaTypeList() {
		super("list", Configuration.CFG_LIST);
	}

	@Override
	public void checkRule(SchemaValidator sv, Configuration cfg, String typeName, String[] typeArgs, String rule)
	        throws ConfigurationException {
		int argsLen;
		String listElementTypeName;
		SchemaType typeDef;

		// --------
		// Check there is one argument.
		// --------
		argsLen = typeArgs.length;
		if (argsLen != 1) {
			throw new ConfigurationException("the '" + typeName + "' type " + "requires one type argument " + "in rule '" + rule + "'");
		}

		// --------
		// The argument must be the name of a string-based type.
		// --------
		listElementTypeName = typeArgs[0];
		typeDef = findType(sv, listElementTypeName);
		if (typeDef == null) {
			throw new ConfigurationException("unknown type '" + listElementTypeName + "' in rule '" + rule + "'");
		}
		switch (typeDef.getCfgType()) {
			case Configuration.CFG_STRING:
				break;
			case Configuration.CFG_LIST:
				throw new ConfigurationException("you cannot embed a list type ('" + listElementTypeName + "') inside " + "another list "
				        + "in rule '" + rule + "'");
			case Configuration.CFG_SCOPE:
				throw new ConfigurationException("you cannot embed a scope type ('" + listElementTypeName + "') inside "
				        + "a list in rule '" + rule + "'");
			default:
				Util.assertion(false);
		}
	}

	@Override
	public void validate(SchemaValidator sv, Configuration cfg, String scope, String name, String typeName, String origTypeName,
	        String[] typeArgs, int indentLevel) throws ConfigurationException {
		String fullyScopedName;
		StringBuffer errSuffix;
		String[] array;
		SchemaType elemTypeDef;
		String elemTypeName;
		String elemValue;
		String sep;
		boolean ok;
		int i;

		Util.assertion(typeArgs.length == 1);
		elemTypeName = typeArgs[0];
		elemTypeDef = findType(sv, elemTypeName);
		Util.assertion(elemTypeDef != null);
		Util.assertion(elemTypeDef.getCfgType() == Configuration.CFG_STRING);

		array = cfg.lookupList(scope, name);
		errSuffix = new StringBuffer();
		for (i = 0; i < array.length; i++) {
			elemValue = array[i];
			ok = callIsA(elemTypeDef, sv, cfg, elemValue, elemTypeName, new String[0], indentLevel + 1, errSuffix);
			if (!ok) {
				if (errSuffix.length() == 0) {
					sep = "";
				} else {
					sep = "; ";
				}
				fullyScopedName = Configuration.mergeNames(scope, name);
				throw new ConfigurationException(cfg.fileName() + ": bad " + elemTypeName + " value ('" + elemValue + "') in the '"
				        + fullyScopedName + "' " + typeName + sep + errSuffix);
			}
		}
	}

}
