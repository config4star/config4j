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


class SchemaTypeTuple extends SchemaType
{

	public SchemaTypeTuple()
	{
		super("tuple", Configuration.CFG_LIST);
	}


	public void checkRule(
		SchemaValidator		sv,
		Configuration		cfg,
		String				typeName,
		String[]			typeArgs,
		String				rule) throws ConfigurationException
	{
		int					i;
		int					argsLen;
		String				elemType;
		SchemaType			typeDef;

		//--------
		// Check there is at least one pair of type and name arguments.
		//--------
		argsLen = typeArgs.length;
		if (argsLen == 0 || (argsLen % 2) != 0) {
			throw new ConfigurationException("the '" + typeName + "' type "
									+ "requires pairs of type and name "
									+ "arguments in rule '" + rule + "'");
		}

		//--------
		// Check that all the type arguments are valid types.
		//--------
		for (i = 0; i < argsLen; i+=2) {
			elemType = typeArgs[i+0];
			typeDef = findType(sv, elemType);
			if (typeDef == null) {
				throw new ConfigurationException("unknown type '" + elemType
									+ "' in rule '" + rule + "'");
			}
			switch (typeDef.getCfgType()) {
			case Configuration.CFG_STRING:
				break;
			case Configuration.CFG_LIST:
				throw new ConfigurationException("you cannot embed a list "
							+ "type ('" + elemType + "') inside a tuple "
							+ "in rule '" + rule + "'");
			case Configuration.CFG_SCOPE:
				throw new ConfigurationException("you cannot embed a scope "
							+ "type ('" + elemType + "') inside a tuple "
							+ "in rule '" + rule + "'");
			default:
				Util.assertion(false); // Bug!
			}
		}
	}


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
		StringBuffer		errSuffix;
		StringBuffer		msg;
		String				fullyScopedName;
		String				elemValue;
		String				elemTypeName;
		String				sep;
		String[]			list;
		int					i;
		int					typeArgsLen;
		int					elemNameIndex;
		int					typeIndex;
		int					rowNum;
		int					numElems;
		SchemaType			elemTypeDef;
		boolean				ok;

		//--------
		// Check the length of the list matches the size of the tuple
		//--------
		typeArgsLen = typeArgs.length;
		Util.assertion(typeArgsLen != 0);
		Util.assertion(typeArgsLen % 2 == 0);
		numElems = typeArgsLen / 2;
		list = cfg.lookupList(scope, name);
		if (list.length != numElems) {
			fullyScopedName = cfg.mergeNames(scope, name);
			msg = new StringBuffer();
			msg.append(cfg.fileName() + ": there should be " + numElems
					+ " entries in the '" + fullyScopedName + "' "
					+ typeName + "; entries denote");
			for (i = 0; i < numElems; i++) {
				msg.append(" '" + typeArgs[i*2 + 0] + "'");
				if (i < numElems-1) {
					msg.append(",");
				}
			}
			throw new ConfigurationException(msg.toString());
		}

		//--------
		// Check each item is of the type specified in the tuple
		//--------
		errSuffix = new StringBuffer();
		for (i = 0; i < list.length; i++) {
			typeIndex     = (i * 2 + 0) % typeArgsLen;
			elemNameIndex = (i * 2 + 1) % typeArgsLen;
			rowNum = (i / numElems) + 1;
			elemValue = list[i];
			elemTypeName = typeArgs[typeIndex];
			elemTypeDef = findType(sv, elemTypeName);
			ok = callIsA(elemTypeDef, sv, cfg, elemValue, elemTypeName,
						new String[0], indentLevel+1, errSuffix);
			if (!ok) {
				if (errSuffix.length() == 0) {
					sep = "";
				} else {
					sep = "; ";
				}
				fullyScopedName = cfg.mergeNames(scope, name);
				throw new ConfigurationException(cfg.fileName() + ": bad "
							+ elemTypeName + " value ('" + elemValue
							+ "') for element " + (i+1) + " ('"
							+ typeArgs[elemNameIndex] + "') of the '"
							+ fullyScopedName + "' " + typeName
							+ sep + errSuffix);
			}
		}
	}

}
