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


class SchemaParser
{
	public SchemaParser(SchemaValidator sv)
	{
		this.sv = sv;
		this.lex = null;
		this.cfg = Configuration.create();
	}

	public void parse(String[] schema) throws ConfigurationException
	{
		int					i;
		int					len;
		String				schemaItem;
		SchemaIdRuleInfo	r1;
		SchemaIdRuleInfo	r2;
		String				s1;
		String				s2;

		sv.sortTypes();
		for (i = 0; i < schema.length; i++) {
			schemaItem = schema[i];
			lex = new SchemaLex(schemaItem);
			token = new LexToken();
			lex.nextToken(token);

			switch (token.getType()) {
			case SchemaLex.LEX_OPTIONAL_SYM:
			case SchemaLex.LEX_REQUIRED_SYM:
			case SchemaLex.LEX_IDENT_SYM:
				sv.schemaIdRules.add(parseIdRule(schemaItem));
				break;
			case SchemaLex.LEX_IGNORE_EVERYTHING_IN_SYM:
			case SchemaLex.LEX_IGNORE_SCOPES_IN_SYM:
			case SchemaLex.LEX_IGNORE_VARIABLES_IN_SYM:
				sv.schemaIgnoreRules.add(parseIgnoreRule(schemaItem));
				break;
			case SchemaLex.LEX_TYPEDEF_SYM:
				parseUserTypeDef(schemaItem);
				sv.sortTypes();
				break;
			default:
				accept(SchemaLex.LEX_IDENT_SYM, schemaItem,
				      "expecting an identifier or '@typedef'");
			}
		}

		//--------
		// Sort the rules.
		//--------
		sv.sortSchemaRules();

		//--------
		// Check if multiple rules have the same name.
		//--------
		len = sv.schemaIdRules.size();
		for (i = 0; i < len-1; i++) {
			r1 = (SchemaIdRuleInfo)sv.schemaIdRules.get(i);
			r2 = (SchemaIdRuleInfo)sv.schemaIdRules.get(i+1);
			s1 = r1.getLocallyScopedName();
			s2 = r2.getLocallyScopedName();
			if (s1.equals(s2)) {
				throw new ConfigurationException("There are "
					+ "multiple rules for '" + s1 + "'");
			}
		}
	}


	//--------------------------------------------------------------
	// BNF for an ignoreRule:
	//  ignoreRule          '@ignoreEverythingIn' locallyScopedName
	//                    | '@ignoreScopesIn'     locallyScopedName
	//                    | '@ignoreVariablesIn'  locallyScopedName
	//  locallyScopedName = IDENT
	//--------------------------------------------------------------

	private SchemaIgnoreRuleInfo parseIgnoreRule(String rule)
											throws ConfigurationException
	{
		String			locallyScopedName;
		short			symbol;

		symbol = (short)token.getType();
		lex.nextToken(token); // consume the "@ignore<something>" keyword
		locallyScopedName = token.getSpelling();
		accept(SchemaLex.LEX_IDENT_SYM, rule, "expecting an identifier");
		accept(SchemaLex.LEX_EOF_SYM,   rule, "expecting <end of string>");
		return new SchemaIgnoreRuleInfo(symbol, locallyScopedName);
	}


	//--------------------------------------------------------------
	// BNF for an idRule:
	// idRule              OptOrRequired locallyScopedName '=' type
	//                   | OptOrRequired locallyScopedName '=' type '[' args ']'
	// OptOrRequired =     '@optional'
	//                   | '@required'
	//                   | empty
	// args =              empty
	//                   | arg { ',' arg }*
	// locallyScopedName = IDENT
	// type    =           IDENT
	// arg     =           IDENT
	//                   | STRING
	//--------------------------------------------------------------

	private SchemaIdRuleInfo parseIdRule(String rule)
												throws ConfigurationException
	{
		SchemaType			typeDef;
		String				locallyScopedName;
		String				typeName;
		ArrayList<String>	args;
		boolean				isOptional;
		String				name;
		int					index;

		switch (token.getType()) {
		case SchemaLex.LEX_REQUIRED_SYM:
			isOptional = false;
			lex.nextToken(token);
			break;
		case SchemaLex.LEX_OPTIONAL_SYM:
			isOptional = true;
			lex.nextToken(token);
			break;
		default:
			isOptional = true;
			break;
		}

		typeName = "";
		args = new ArrayList<String>();
		locallyScopedName = token.getSpelling();
		accept(SchemaLex.LEX_IDENT_SYM, rule, "expecting an identifier");

		//--------
		// Complain if we have @required uid-<something>
		//--------
		if (!isOptional) {
			index = locallyScopedName.lastIndexOf('.');
			name = locallyScopedName.substring(index + 1);
			if (name.startsWith("uid-")) {
				throw new ConfigurationException("Use of '@required' is "
							+ "incompatible with the uid- entry ('" + name
							+ "') in rule '" + rule + "'");
			}
		}

		accept(SchemaLex.LEX_EQUALS_SYM, rule, "expecting '='");

		typeName = token.getSpelling();
		accept(SchemaLex.LEX_IDENT_SYM, rule,"expecting an identifier");

		typeDef = sv.findType(typeName);
		if (typeDef == null) {
			throw new ConfigurationException("Unknown type '" + typeName
												+ "' in rule '" + rule + "'");
		}
		if (token.getType() == SchemaLex.LEX_EOF_SYM) {
			sv.callCheckRule(typeDef, cfg, typeName, args, rule, 1);
			return new SchemaIdRuleInfo(locallyScopedName, typeName, args,
										isOptional);
		}

		accept(SchemaLex.LEX_OPEN_BRACKET_SYM, rule, "expecting '['");
		if (token.getType() == SchemaLex.LEX_IDENT_SYM
		    || token.getType() == SchemaLex.LEX_STRING_SYM)
		{
			args.add(token.getSpelling());
			lex.nextToken(token);
		} else if (token.getType() != SchemaLex.LEX_CLOSE_BRACKET_SYM) {
			accept(SchemaLex.LEX_IDENT_SYM, rule,
			      "expecting an identifier, string or ']'");
		}
		while (token.getType() != SchemaLex.LEX_CLOSE_BRACKET_SYM) {
			accept(SchemaLex.LEX_COMMA_SYM, rule, "expecting ','");
			args.add(token.getSpelling());
			if (token.getType() == SchemaLex.LEX_IDENT_SYM
			    || token.getType() == SchemaLex.LEX_STRING_SYM)
			{
				lex.nextToken(token);
			} else {
				accept(SchemaLex.LEX_IDENT_SYM, rule,
				      "expecting an identifier, string or ']'");
			}
		}
		accept(SchemaLex.LEX_CLOSE_BRACKET_SYM, rule, "expecting ']'");
		accept(SchemaLex.LEX_EOF_SYM, rule, "expecting <end of string>");
		sv.callCheckRule(typeDef, cfg, typeName, args, rule, 1);
		return new SchemaIdRuleInfo(locallyScopedName, typeName, args,
									isOptional);
	}


	private void parseUserTypeDef(String str) throws ConfigurationException
	{
		SchemaType			baseTypeDef;
		String				typeName;
		String				baseTypeName;
		ArrayList<String>	baseTypeArgs;

		baseTypeArgs = new ArrayList<String>();
		accept(SchemaLex.LEX_TYPEDEF_SYM, str, "expecting '@typedef'");
		typeName = token.getSpelling();
		accept(SchemaLex.LEX_IDENT_SYM, str, "expecting an identifier");
		accept(SchemaLex.LEX_EQUALS_SYM, str, "expecting '='");
		baseTypeName = token.getSpelling();
		accept(SchemaLex.LEX_IDENT_SYM, str, "expecting an identifier");

		baseTypeDef = sv.findType(baseTypeName);
		if (baseTypeDef == null) {
			throw new ConfigurationException("Unknown type '" + baseTypeName
							+ "' in user-type " + "definition '" + str + "'");
		}
		if (token.getType() == SchemaLex.LEX_EOF_SYM) {
			//--------
			// Finished. Ask the base type to check its (empty) arguments
			// and then register the new command.
			//--------
			sv.callCheckRule(baseTypeDef, cfg, baseTypeName, baseTypeArgs, str,
								1);
			sv.registerTypedef(typeName, baseTypeDef.getCfgType(),
							   baseTypeName, baseTypeArgs);
			return;
		}

		accept(SchemaLex.LEX_OPEN_BRACKET_SYM, str, "expecting '['");
		if (token.getType() == SchemaLex.LEX_IDENT_SYM
		    || token.getType() == SchemaLex.LEX_STRING_SYM)
		{
			baseTypeArgs.add(token.getSpelling());
			lex.nextToken(token);
		} else if (token.getType() != SchemaLex.LEX_CLOSE_BRACKET_SYM) {
			accept(SchemaLex.LEX_IDENT_SYM, str,
			      "expecting an identifier, string or ']'");
		}
		while (token.getType() != SchemaLex.LEX_CLOSE_BRACKET_SYM) {
			accept(SchemaLex.LEX_COMMA_SYM, str, "expecting ','");
			baseTypeArgs.add(token.getSpelling());
			if (token.getType() == SchemaLex.LEX_IDENT_SYM
			    || token.getType() == SchemaLex.LEX_STRING_SYM)
			{
				lex.nextToken(token);
			} else {
				accept(SchemaLex.LEX_IDENT_SYM, str,
				      "expecting an identifier, string or ']'");
			}
		}
		accept(SchemaLex.LEX_CLOSE_BRACKET_SYM, str, "expecting ']'");
		accept(SchemaLex.LEX_EOF_SYM, str, "expecting <end of string>");

		//--------
		// Finished. Ask the base type to check its arguments and then
		// register the new command.
		//--------
		sv.callCheckRule(baseTypeDef, cfg, baseTypeName, baseTypeArgs, str, 1);
		sv.registerTypedef(typeName, baseTypeDef.getCfgType(), baseTypeName,
						   baseTypeArgs);
	}


	private void accept(int sym, String rule, String msgPrefix)
												throws ConfigurationException
	{
		if (token.getType() == sym) {
			lex.nextToken(token);
		} else {
			throw new ConfigurationException("error in validation rule '"
							+ rule + "': " + msgPrefix + " near '"
							+ token.getSpelling() + "'");
		}
	}

	//--------
	// Instance variables
	//--------
	private SchemaLex				lex;
	private LexToken				token;
	private SchemaValidator			sv;
	private Configuration			cfg;

}
