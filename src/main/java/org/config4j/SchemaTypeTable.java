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

class SchemaTypeTable extends SchemaType {

	public SchemaTypeTable() {
		super("table", Configuration.CFG_LIST);
	}

	@Override
	public void checkRule(SchemaValidator sv, Configuration cfg, String typeName, String[] typeArgs, String rule)
	        throws ConfigurationException {
		int i;
		int argsLen;
		String columnType;
		SchemaType typeDef;

		// --------
		// Check there is at least one pair of
		// column-type, column-name arguments.
		// --------
		argsLen = typeArgs.length;
		if (argsLen == 0 || argsLen % 2 != 0) {
			throw new ConfigurationException("the '" + typeName + "' type " + "requires pairs of column-type and column-name "
			        + "arguments in rule '" + rule + "'");
		}

		// --------
		// Check that all the column-type arguments are valid types.
		// --------
		for (i = 0; i < argsLen; i += 2) {
			columnType = typeArgs[i + 0];
			typeDef = findType(sv, columnType);
			if (typeDef == null) {
				throw new ConfigurationException("unknown type '" + columnType + "' in rule '" + rule + "'");
			}
			switch (typeDef.getCfgType()) {
				case Configuration.CFG_STRING:
					break;
				case Configuration.CFG_LIST:
					throw new ConfigurationException("you cannot embed a list type " + "('" + columnType + "') inside a table in rule '"
					        + rule + "'");
				case Configuration.CFG_SCOPE:
					throw new ConfigurationException("you cannot embed a scope " + "type ('" + columnType + "') inside a table "
					        + "in rule '" + rule + "'");
				default:
					Util.assertion(false); // Bug!
			}
		}
	}

	@Override
	public void validate(SchemaValidator sv, Configuration cfg, String scope, String name, String typeName, String origTypeName,
	        String[] typeArgs, int indentLevel) throws ConfigurationException {
		StringBuffer errSuffix;
		StringBuffer msg;
		String fullyScopedName;
		String colValue;
		String colTypeName;
		String sep;
		String[] list;
		int i;
		int typeArgsLen;
		int colNameIndex;
		int typeIndex;
		int rowNum;
		int numColumns;
		SchemaType colTypeDef;
		boolean ok;

		// --------
		// Check that the length of the list is a multiple of the number
		// of columns in the table.
		// --------
		typeArgsLen = typeArgs.length;
		Util.assertion(typeArgsLen != 0);
		Util.assertion(typeArgsLen % 2 == 0);
		numColumns = typeArgsLen / 2;
		list = cfg.lookupList(scope, name);
		if (list.length % numColumns != 0) {
			fullyScopedName = Configuration.mergeNames(scope, name);
			msg = new StringBuffer();
			msg.append(cfg.fileName() + ": the number of entries in the '" + fullyScopedName + "' " + typeName + " is not a multiple "
			        + "of " + numColumns);
			throw new ConfigurationException(msg.toString());
		}

		// --------
		// Check each item in the list is of the type specified for its column
		// --------
		errSuffix = new StringBuffer();
		for (i = 0; i < list.length; i++) {
			typeIndex = (i * 2 + 0) % typeArgsLen;
			colNameIndex = (i * 2 + 1) % typeArgsLen;
			rowNum = i / numColumns + 1;
			colValue = list[i];
			colTypeName = typeArgs[typeIndex];
			colTypeDef = findType(sv, colTypeName);
			ok = callIsA(colTypeDef, sv, cfg, colValue, colTypeName, new String[0], indentLevel + 1, errSuffix);
			if (!ok) {
				if (errSuffix.length() == 0) {
					sep = "";
				} else {
					sep = "; ";
				}
				fullyScopedName = Configuration.mergeNames(scope, name);
				throw new ConfigurationException(cfg.fileName() + ": bad " + colTypeName + " value ('" + colValue + "') for the '"
				        + typeArgs[colNameIndex] + "' column in row " + rowNum + " of the '" + fullyScopedName + "' " + typeName + sep
				        + errSuffix);
			}
		}
	}

}
