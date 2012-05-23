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


public class Config2J
{

	static private String calculateRuleForName(
		Configuration		cfg,
		String				name,
		String				uName,
		String[]			wildcardedNamesAndTypes)
	{
		int					i;
		String				str;
		String				keyword;
		String				wildcardedName;
		String				type;
		String				rule;

		for (i = 0; i < wildcardedNamesAndTypes.length; i+= 3) {
			keyword        = wildcardedNamesAndTypes[i+0]; //@optional/@required
			wildcardedName = wildcardedNamesAndTypes[i+1];
			type           = wildcardedNamesAndTypes[i+2];
			if (Configuration.patternMatch(uName, wildcardedName)) {
				rule = keyword + " " + uName + " = " + type;
				return rule;
			}
		}

		//--------
		// Use heuristics to guess a good type.
		//--------
		if (cfg.type("", name) == Configuration.CFG_SCOPE) {
			rule = uName + " = scope";
		} else if (cfg.type("", name) == Configuration.CFG_LIST) {
			rule = uName + " = list[string]";
		} else {
			str = cfg.lookupString("", name);
			if (cfg.isBoolean(str)) {
				rule = uName + " = boolean";
			} else if (cfg.isInt(str)) {
				rule = uName + " = int";
			} else if (cfg.isFloat(str)) {
				rule = uName + " = float";
			} else if (cfg.isDurationSeconds(str)) {
				rule = uName + " = durationSeconds";
			} else if (cfg.isDurationMilliseconds(str)) {
				rule = uName + " = durationMilliseconds";
			} else if (cfg.isDurationMicroseconds(str)) {
				rule = uName + " = durationMicroseconds";
			} else if (cfg.isMemorySizeBytes(str)) {
				rule = uName + " = memorySizeBytes";
			} else if (cfg.isMemorySizeKB(str)) {
				rule = uName + " = memorySizeKB";
			} else if (cfg.isMemorySizeMB(str)) {
				rule = uName + " = memorySizeMB";
			} else {
				rule = uName + " = string";
			}
		}
		return rule;
	}


	private static boolean doesVectorcontainString(
		ArrayList			vec,
		String				str)
	{
		int					i;
		int					len;
		String				item;

		len = vec.size();
		for (i = 0; i < len; i++) {
			item = (String)vec.get(i);
			if (item.equals(str)) {
				return true;
			}
		}
		return false;
	}


	private static String[] calculateSchema(
		Configuration		cfg,
		String[]			namesList,
		String[]			recipeUserTypes,
		String[]			wildcardedNamesAndTypes,
		String[]			recipeIgnoreRules)
												throws ConfigurationException
	{
		int					i;
		int					len;
		String				rule;
		String				name;
		String				uName;
		ArrayList			uidNames;
		ArrayList			schema;

		uidNames = new ArrayList();
		schema = new ArrayList();
		for (i = 0; i < recipeIgnoreRules.length; i++) {
			schema.add(recipeIgnoreRules[i]);
		}
		for (i = 0; i < recipeUserTypes.length; i++) {
			schema.add(recipeUserTypes[i]);
		}
		for (i = 0; i < namesList.length; i++) {
			name = namesList[i];
			if (name.equals("uid-")) {
				rule = calculateRuleForName(cfg, name, name,
											wildcardedNamesAndTypes);
				schema.add(rule);
			} else {
				uName = cfg.unexpandUid(name);
				if (!doesVectorcontainString(uidNames, uName)) {
					uidNames.add(uName);
					rule = calculateRuleForName(cfg, name, uName,
												wildcardedNamesAndTypes);
					schema.add(rule);
				}
			}
		}
		return (String[])schema.toArray(new String[schema.size()]);
	}


	private static boolean doesPatternMatchAnyUnexpandedNameInList(
		Configuration		cfg,
		String				pattern,
		String[]			namesList)
	{
		int					i;
		String				uName;
		StringBuffer		buf;

		for (i = 0; i < namesList.length; i++) {
			uName = cfg.unexpandUid(namesList[i]);
			if (Configuration.patternMatch(uName, pattern)) {
				return true;
			}
		}
		return false;
	}


	private static String[] checkForUnmatchedPatterns(
		Configuration		cfg,
		String[]	 		namesList,
		String[]			wildcardedNamesAndTypes)
												throws ConfigurationException
	{
		int					i;
		int					len;
		String				wildcardedName;
		ArrayList			unmatchedPatterns;

		unmatchedPatterns = new ArrayList();
		//--------
		// Check if there is a wildcarded name that does not match
		// anything
		//--------
		for (i = 0; i < wildcardedNamesAndTypes.length; i += 3) {
			wildcardedName = wildcardedNamesAndTypes[i+1];
			if (!doesPatternMatchAnyUnexpandedNameInList(cfg, wildcardedName,
														 namesList))
			{
				unmatchedPatterns.add(wildcardedName);
			}
		}
		return (String[])unmatchedPatterns.toArray(
										new String[unmatchedPatterns.size()]);
	}


	public static void main(String[] args)
	{
		boolean				ok;
		Configuration		cfg;
		Configuration		schemaCfg;
		Config2JUtil		util;
		String[]			namesList;
		String[]			recipeUserTypes;
		String[]			wildcardedNamesAndTypes;
		String[]			recipeIgnoreRules;
		String[]			schema;
		String[]			unmatchedPatterns;
		String				scope;
		SchemaValidator		sv = new SchemaValidator();
		int					i;
		int					len;
		String[]			overrideSchema = new String[] {
					"@typedef keyword = enum[\"@optional\", \"@required\"]",
					"user_types = list[string]",
					"wildcarded_names_and_types = table[keyword,keyword, "
									+ "string,wildcarded-name, string,type]",
					"ignore_rules = list[string]",
							};

		schema = null;
		unmatchedPatterns = new String[0];
		util = new Config2JUtil("org.config4j.Config2J");
		ok = util.parseCmdLineArgs(args);
		cfg = Configuration.create();
		schemaCfg = Configuration.create();
		if (ok && util.wantSchema()) {
			try {
				cfg.parse(util.getCfgFileName());
				namesList = cfg.listFullyScopedNames("", "",
									Configuration.CFG_SCOPE_AND_VARS, true);
				if (util.getSchemaOverrideCfg() != null) {
					schemaCfg.parse(util.getSchemaOverrideCfg());
					scope = util.getSchemaOverrideScope();
					sv.parseSchema(overrideSchema);
					sv.validate(schemaCfg, scope, "");
					recipeUserTypes = schemaCfg.lookupList(scope, "user_types");
					wildcardedNamesAndTypes = schemaCfg.lookupList(scope,
												"wildcarded_names_and_types");
					recipeIgnoreRules = schemaCfg.lookupList(scope,
															 "ignore_rules");
				} else {
					recipeUserTypes = new String[0];
					wildcardedNamesAndTypes = new String[0];
					recipeIgnoreRules = new String[0];
				}
				schema = calculateSchema(cfg, namesList, recipeUserTypes,
								 wildcardedNamesAndTypes, recipeIgnoreRules);
				unmatchedPatterns = checkForUnmatchedPatterns(cfg, namesList,
													wildcardedNamesAndTypes);
			} catch(ConfigurationException ex) {
				System.err.println(ex.getMessage());
				ok = false;
			}
			len = unmatchedPatterns.length;
			if (len != 0) {
				System.err.println("Error: the following patterns in the "
						+ "schema recipe did not match anything");
				for (i = 0; i < len; i++) {
					System.err.println("\t'" + unmatchedPatterns[i] + "'");
				}
				ok = false;
			}
		}

		if (ok) {
			ok = util.generateJavaClass(schema);
		}

		if (ok) {
			System.exit(0);
		} else {
			System.exit(1);
		}
	}

}
