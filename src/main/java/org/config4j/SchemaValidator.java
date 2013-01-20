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

import java.util.ArrayList;
import java.util.Arrays;


public class SchemaValidator
{

	public static final int DO_NOT_FORCE   = 0;
	public static final int FORCE_OPTIONAL = 1;
	public static final int FORCE_REQUIRED = 2;


	public SchemaValidator()
	{
		try {
			wantDiagnostics      = false;
			schemaIdRules        = new ArrayList<SchemaIdRuleInfo>();
			schemaIgnoreRules    = new ArrayList<SchemaIgnoreRuleInfo>();
			schemaTypes          = new ArrayList<SchemaType>();
			areSchemaTypesSorted = false;
			registerBuiltinTypes();
		} catch(ConfigurationException ex) {
			Util.assertion(false);
		}
	}


	public void setWantDiagnostics(boolean wantDiagnostics)
	{
		this.wantDiagnostics = wantDiagnostics;
	}


	public boolean getWantDiagnostics()
	{
		return wantDiagnostics;
	}


	public void parseSchema(String[] schema) throws ConfigurationException
	{
		SchemaParser	schemaParser = new SchemaParser(this);
		String			prefix = "---- " + this.getClass().getName()
				+ ".parseSchema()";
		if (wantDiagnostics) {
			System.out.print("\n" + prefix + ": start\n");
		}
		try {
			schemaParser.parse(schema);
		} catch(ConfigurationException ex) {
			if (wantDiagnostics) {
				System.out.print("\n" + prefix + ": error: "
						+ ex.getMessage() + "\n");
			}
			throw ex;
		}
		if (wantDiagnostics) {
			System.out.print("\n" + prefix + ": end\n");
		}
	}


	public void validate(
			Configuration			cfg,
			String					scope,
			String					name) throws ConfigurationException
			{
		validate(cfg, scope, name, true, Configuration.CFG_SCOPE_AND_VARS,
				DO_NOT_FORCE);
			}


	public void validate(
			Configuration			cfg,
			String					scope,
			String					name,
			int						forceMode) throws ConfigurationException
			{
		validate(cfg, scope, name, true, Configuration.CFG_SCOPE_AND_VARS,
				forceMode);
			}


	public void validate(
			Configuration			cfg,
			String					scope,
			String					name,
			boolean					recurseIntoSubscopes,
			int						typeMask) throws ConfigurationException
			{
		validate(cfg, scope, name, recurseIntoSubscopes, typeMask,
				DO_NOT_FORCE);
			}


	public void validate(
			Configuration			cfg,
			String					scope,
			String					name,
			boolean					recurseIntoSubscopes,
			int						typeMask,
			int						forceMode) throws ConfigurationException
			{
		String					fullyScopedName;
		String[]				itemNames;

		//--------
		// Get a list of the entries in the scope
		//--------
		fullyScopedName = cfg.mergeNames(scope, name);
		itemNames = cfg.listLocallyScopedNames(scope, name, typeMask,
				recurseIntoSubscopes);

		//--------
		// Now validate those names
		//--------
		validate(cfg, scope, name, itemNames, forceMode);
			}


	private void checkTypeDoesNotExist(String typeName)
			throws ConfigurationException
			{
		int						len;
		int						i;
		SchemaType				type;

		len = schemaTypes.size();
		for (i = 0; i < len; i++) {
			type = schemaTypes.get(i);
			if (typeName.equals(type.getTypeName())) {
				throw new ConfigurationException("schema type '" + typeName
						+ "' is already registered");
			}
		}
			}


	protected void registerType(SchemaType type) throws ConfigurationException
	{
		checkTypeDoesNotExist(type.getTypeName());
		schemaTypes.add(type);
		areTypesSorted = false;
	}


	void registerTypedef(
String typeName, int cfgType, String baseTypeName, ArrayList<String> baseTypeArgsList)
	        throws ConfigurationException
			{
		SchemaType			type;
		String[]			baseTypeArgs;

		checkTypeDoesNotExist(typeName);
		baseTypeArgs = baseTypeArgsList.toArray(new String[0]);
		type = new SchemaTypeTypedef(typeName, cfgType, baseTypeName,
				baseTypeArgs);
		schemaTypes.add(type);
		areTypesSorted = false;
			}


	void sortTypes()
	{
		SchemaType[]		array;
		int					i;

		array = new SchemaType[schemaTypes.size()];
		schemaTypes.toArray(array);
		Arrays.sort(array);
		schemaTypes.clear();
		schemaTypes.ensureCapacity(array.length);
		for (i = 0; i < array.length; i++) {
			schemaTypes.add(array[i]);
		}
		areTypesSorted = true;
	}


	SchemaType findType(String name)
	{
		SchemaType				search;
		SchemaType[]			array;
		int						index;

		search = new SchemaTypeDummy(name);
		if (areTypesSorted) {
			array = new SchemaType[schemaTypes.size()];
			schemaTypes.toArray(array);
			index = Arrays.binarySearch(array, search);
		} else {
			index = schemaTypes.indexOf(search);
		}
		if (index < 0) {
			return null;
		}
		return schemaTypes.get(index);
	}


	private boolean wantDiagnostics;


	private void indent(int indentLevel)
	{
		for (int i = 0; i < indentLevel; i++) {
			System.out.print("  ");
		}
	}


	private void printTypeArgs(
			String[]			typeArgs,
			int					indentLevel)
	{
		indent(indentLevel);
		System.out.print("typeArgs = [");
		for (int i = 0; i < typeArgs.length; i++) {
			System.out.print("\"" + typeArgs[i] + "\"");
			if (i < typeArgs.length-1) {
				System.out.print(", ");
			}
		}
		System.out.print("]\n");
	}


	private void printTypeNameAndArgs(
			String				typeName,
			String[]			typeArgs,
			int					indentLevel)
	{
		indent(indentLevel);
		System.out.print("typename = \"" + typeName + "\"; typeArgs = [");
		for (int i = 0; i < typeArgs.length; i++) {
			System.out.print("\"" + typeArgs[i] + "\"");
			if (i < typeArgs.length-1) {
				System.out.print(", ");
			}
		}
		System.out.print("]\n");
	}


	void callCheckRule(
			SchemaType			target,
			Configuration		cfg,
			String				typeName,
			ArrayList<String>	typeArgsList,
			String				rule,
			int					indentLevel)
	{
		String[]			typeArgs;

		typeArgs = typeArgsList.toArray(new String[0]);
		callCheckRule(target, cfg, typeName, typeArgs, rule, indentLevel);
	}


	void callCheckRule(
			SchemaType			target,
			Configuration		cfg,
			String				typeName,
			String[]			typeArgs,
			String				rule,
			int					indentLevel)
	{
		try {
			if (wantDiagnostics) {
				System.out.println("");
				indent(indentLevel);
				System.out.print("start " + target.getClassName()
						+ ".checkRule()\n");
				indent(indentLevel+1);
				System.out.print("rule = \"" + rule + "\"\n");
				printTypeNameAndArgs(typeName, typeArgs, indentLevel+1);
			}
			target.checkRule(this, cfg, typeName, typeArgs, rule);
			if (wantDiagnostics) {
				indent(indentLevel);
				System.out.print("end " + target.getClassName()
						+ ".checkRule()\n");
			}
		} catch(ConfigurationException ex) {
			if (wantDiagnostics) {
				System.out.println("");
				indent(indentLevel);
				System.out.print("exception throw from " + target.getClassName()
						+ ".checkRule(): " + ex.getMessage() + "\n");
			}
			throw ex;
		}
	}


	void callValidate(
			SchemaType			target,
			Configuration		cfg,
			String				scope,
			String				name,
			String				typeName,
			String				origTypeName,
			String[]			typeArgs,
			int					indentLevel)
	{
		try {
			if (wantDiagnostics) {
				System.out.println("");
				indent(indentLevel);
				System.out.print("start " + target.getClassName()
						+ ".validate()\n");
				indent(indentLevel+1);
				System.out.print("scope = \"" + scope + "\"; name = \""
						+ name + "\"\n");
				indent(indentLevel+1);
				System.out.print("typeName = \"" + typeName
						+ "\"; origTypeName = \"" + origTypeName + "\"\n");
				printTypeArgs(typeArgs, indentLevel+1);
			}
			target.validate(this, cfg, scope, name, typeName, origTypeName,
					typeArgs, indentLevel+1);
			if (wantDiagnostics) {
				indent(indentLevel);
				System.out.print("end " + target.getClassName()
						+ ".validate()\n");
			}
		} catch(ConfigurationException ex) {
			if (wantDiagnostics) {
				System.out.println("");
				indent(indentLevel);
				System.out.print("exception throw from " + target.getClassName()
						+ ".validate(): " + ex.getMessage() + "\n");
			}
			throw ex;
		}
	}


	boolean callIsA(
			SchemaType			target,
			Configuration		cfg,
			String				value,
			String				typeName,
			String[]			typeArgs,
			int					indentLevel,
			StringBuffer		errSuffix)
	{
		boolean				result = false;

		try {
			if (wantDiagnostics) {
				System.out.println("");
				indent(indentLevel);
				System.out.print("start " + target.getClassName() + ".isA()\n");
				indent(indentLevel+1);
				System.out.print("value = \"" + value + "\"\n");
				printTypeNameAndArgs(typeName, typeArgs, indentLevel+1);
			}
			result = target.isA(this, cfg, value, typeName, typeArgs,
					indentLevel+1, errSuffix);
			if (wantDiagnostics) {
				indent(indentLevel);
				System.out.print("end " + target.getClassName() + ".isA()\n");
				indent(indentLevel+1);
				System.out.print("result = " + result + "; errSuffix = \""
						+ errSuffix + "\"\n");
			}
		} catch(ConfigurationException ex) {
			if (wantDiagnostics) {
				System.out.println("");
				indent(indentLevel);
				System.out.print("exception throw from " + target.getClassName()
						+ ".isA(): " + ex.getMessage() + "\n");
			}
			throw ex;
		}
		return result;
	}


	private void validate(
			Configuration		cfg,
			String				scope,
			String				localName,
			String[]			itemNames,
			int					forceMode) throws ConfigurationException
			{
		String				fullyScopedName;
		String				unlistedName;
		String				unexpandedName;
		String				typeName;
		String				iName;
		String				msg;
		int					i;
		SchemaIdRuleInfo	idRule;
		SchemaType			typeDef;
		String				prefix = "---- " + this.getClass().getName()
				+ ".validate()";

		fullyScopedName = cfg.mergeNames(scope, localName);
		if (wantDiagnostics) {
			System.out.print("\n" + prefix + ": start\n");
		}
		//--------
		// Compare every name in itemNames with ignoreIdRules and idRules
		//--------
		for (i = 0; i < itemNames.length; i++) {
			iName = itemNames[i];
			unexpandedName = cfg.unexpandUid(iName);
			if (shouldIgnore(cfg, scope, iName, unexpandedName)) {
				if (wantDiagnostics) {
					System.out.print("\n ignoring '" + iName + "'\n");
				}
				continue;
			}
			idRule = findIdRule(unexpandedName);
			if (idRule == null) {
				//--------
				// Can't find an idRule for the entry
				//--------
				unlistedName = cfg.mergeNames(fullyScopedName, iName);
				msg = null;
				switch (cfg.type(unlistedName, "")) {
				case Configuration.CFG_SCOPE:
					msg = cfg.fileName() + ": the '" + unlistedName
					+ "' scope is unknown.";
					break;
				case Configuration.CFG_LIST:
				case Configuration.CFG_STRING:
					msg = cfg.fileName() + ": the '" + unlistedName
					+ "' variable is unknown.";
					break;
				default:
					Util.assertion(false); // Bug!
				}
				if (wantDiagnostics) {
					System.out.print("\n" + prefix + ": error: " + msg + "\n");
				}
				throw new ConfigurationException(msg);
			}

			//--------
			// There is an idRule for the entry. Look up the rule's type,
			// ad invoke its validate() operation.
			//--------
			typeName = idRule.getTypeName();
			typeDef = findType(typeName);
			Util.assertion(typeDef != null);
			try {
				callValidate(typeDef, cfg, fullyScopedName, iName, typeName,
						typeName, idRule.getArgs(), 1);
			} catch(ConfigurationException ex) {
				if (wantDiagnostics) {
					System.out.print("\n" + prefix + ": end\n\n");
				}
				throw ex;
			}
		}

		validateForceMode(cfg, scope, localName, forceMode);

		if (wantDiagnostics) {
			System.out.print("\n" + prefix + ": end\n\n");
		}
			}


	private void validateForceMode(
			Configuration			cfg,
			String					scope,
			String					localName,
			int						forceMode) throws ConfigurationException
			{
		int						i;
		int						len;
		boolean					isOptional;
		String					fullyScopedName;
		String					nameOfMissingEntry;
		String					typeName;
		String					nameInRule;
		SchemaIdRuleInfo		idRule;

		if (forceMode == FORCE_OPTIONAL) {
			return;
		}
		fullyScopedName = cfg.mergeNames(scope, localName);
		len = schemaIdRules.size();
		for (i = 0; i < len; i++) {
			idRule = schemaIdRules.get(i);
			isOptional = idRule.getIsOptional();
			if (forceMode == DO_NOT_FORCE && isOptional) {
				continue;
			}
			nameInRule = idRule.getLocallyScopedName();
			if (nameInRule.indexOf("uid-") != -1) {
				validateRequiredUidEntry(cfg, fullyScopedName, idRule);
			} else {
				if (cfg.type(fullyScopedName, nameInRule)
						== Configuration.CFG_NO_VALUE)
				{
					nameOfMissingEntry = cfg.mergeNames(fullyScopedName,
							nameInRule);
					typeName = idRule.getTypeName();
					throw new ConfigurationException(cfg.fileName() + ": the "
							+ typeName + " '" + nameOfMissingEntry
							+ "' does not exist");
				}
			}
		}
			}


	private void validateRequiredUidEntry(
			Configuration			cfg,
			String					fullScope,
			SchemaIdRuleInfo		idRule) throws ConfigurationException
			{
		String					nameInRule;
		int						lastDotIndex;
		StringBuffer			parentScopePattern = new StringBuffer();
		String					nameOfMissingEntry;
		String[]				parentScopes;
		String					typeName;
		String					lastPartOfName;
		int						i;

		nameInRule = idRule.getLocallyScopedName();
		Util.assertion(nameInRule.indexOf("uid-") != -1);
		lastDotIndex = nameInRule.lastIndexOf('.');
		if (lastDotIndex == -1
				|| nameInRule.indexOf("uid-", lastDotIndex+1) != -1)
		{
			return;
		}

		parentScopePattern.append(fullScope);
		if (fullScope.length() != 0) {
			parentScopePattern.append(".");
		}
		parentScopePattern.append(nameInRule.substring(0, lastDotIndex));
		parentScopes = cfg.listFullyScopedNames(fullScope, "",
				Configuration.CFG_SCOPE, true,
				parentScopePattern.toString());
		lastPartOfName = nameInRule.substring(lastDotIndex + 1);
		for (i = 0; i < parentScopes.length; i++) {
			if (cfg.type(parentScopes[i], lastPartOfName)
					== Configuration.CFG_NO_VALUE)
			{
				nameOfMissingEntry = cfg.mergeNames(parentScopes[i],
						lastPartOfName);
				typeName = idRule.getTypeName();
				throw new ConfigurationException(cfg.fileName() + ": the "
						+ typeName + " '" + nameOfMissingEntry
						+ "' does not exist");
			}
		}

			}


	private boolean shouldIgnore(
			Configuration			cfg,
			String					scope,
			String					expandedName,
			String					unexpandedName)
	{
		int						i;
		int						len;
		int						size;
		short					symbol;
		String					name;
		String					nameAfterPrefix;
		SchemaIgnoreRuleInfo	rule;
		boolean					hasDotAfterPrefix;
		int						cfgType = 0;

		size = schemaIgnoreRules.size();
		for (i = 0; i < size; i++) {
			rule = schemaIgnoreRules.get(i);
			name = rule.getLocallyScopedName();
			len = name.length();
			//--------
			// Does unexpandedName start with rule.locallScopedName
			// followed by "."?
			//--------
			if (!unexpandedName.startsWith(name)) {
				continue;
			}
			if (unexpandedName.length() == len
					|| unexpandedName.charAt(len) != '.')
			{
				continue;
			}

			//--------
			// It does. Whether we ignore the item depends on the
			// "@ignore<something>" keyword used.
			//--------
			symbol = rule.getSymbol();
			switch (symbol) {
			case SchemaLex.LEX_IGNORE_EVERYTHING_IN_SYM:
				return true;
			case SchemaLex.LEX_IGNORE_SCOPES_IN_SYM:
			case SchemaLex.LEX_IGNORE_VARIABLES_IN_SYM:
				break;
			default:
				Util.assertion(false, "Bug!");
				break;
			}
			nameAfterPrefix = unexpandedName.substring(len+1);
			hasDotAfterPrefix = (nameAfterPrefix.indexOf('.') != -1);
			try {
				cfgType = cfg.type(scope, expandedName);
			} catch(ConfigurationException ex) {
				Util.assertion(false, "Bug!");
			}
			if (symbol == SchemaLex.LEX_IGNORE_VARIABLES_IN_SYM) {
				if (hasDotAfterPrefix) {
					//--------
					// The item is a variable in a nested scope so
					// the "@ignoreVariablesIn" rule does not apply.
					//--------
					continue;
				}
				//--------
				// The item is directly in the scope, so the
				// "@ignoreVariablesIn" rule applies if the item
				// is a variable.
				//--------
				if ((cfgType & Configuration.CFG_VARIABLES) != 0) {
					return true;
				} else {
					continue;
				}
			}

			Util.assertion(symbol == SchemaLex.LEX_IGNORE_SCOPES_IN_SYM);
			if (hasDotAfterPrefix) {
				//--------
				// The item is in a *nested* scope, so we ignore it.
				//--------
				return true;
			}
			//--------
			// The item is directly in the ignorable-able scope,
			// so we ignore it only if the item is a scope.
			//--------
			if (cfgType == Configuration.CFG_SCOPE) {
				return true;
			} else {
				continue;
			}
		}
		return false;
	}

	//--------
	// Helper operations.
	//--------

	void sortSchemaRules()
	{
		SchemaIdRuleInfo[]		array;
		int						i;

		array = new SchemaIdRuleInfo[schemaIdRules.size()];
		schemaIdRules.toArray(array);
		Arrays.sort(array);
		schemaIdRules.clear();
		schemaIdRules.ensureCapacity(array.length);
		for (i = 0; i < array.length; i++) {
			schemaIdRules.add(array[i]);
		}
	}



	private SchemaIdRuleInfo findIdRule(String name)
	{
		int					index;
		SchemaIdRuleInfo	search;

		search = new SchemaIdRuleInfo(name, null, (String[])null, false);
		index = schemaIdRules.indexOf(search);
		if (index < 0) {
			return null;
		}
		return schemaIdRules.get(index);
	}


	//--------
	// Instance variables NOT visible to subclasses.
	//--------
	ArrayList<SchemaIdRuleInfo>				schemaIdRules;
	ArrayList<SchemaIgnoreRuleInfo>			schemaIgnoreRules;
	ArrayList<SchemaType>					schemaTypes;
	boolean									areSchemaTypesSorted;
	boolean									areTypesSorted;

	private void registerBuiltinTypes()
	{
		registerType(new SchemaTypeScope());

		//--------
		// List-based types
		//--------
		registerType(new SchemaTypeList());
		registerType(new SchemaTypeTable());
		registerType(new SchemaTypeTuple());

		//--------
		// String-based types
		//--------
		registerType(new SchemaTypeString());
		registerType(new SchemaTypeBoolean());
		registerType(new SchemaTypeDurationMicroseconds());
		registerType(new SchemaTypeDurationMilliseconds());
		registerType(new SchemaTypeDurationSeconds());
		registerType(new SchemaTypeEnum());
		registerType(new SchemaTypeFloat());
		registerType(new SchemaTypeFloatWithUnits());
		registerType(new SchemaTypeInt());
		registerType(new SchemaTypeIntWithUnits());
		registerType(new SchemaTypeUnitsWithFloat());
		registerType(new SchemaTypeUnitsWithInt());
		registerType(new SchemaTypeMemorySizeBytes());
		registerType(new SchemaTypeMemorySizeKB());
		registerType(new SchemaTypeMemorySizeMB());
	}

}
