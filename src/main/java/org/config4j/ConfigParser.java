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

//-----------------------------------------------------------------------
//
// BNF of config file
// ------------------
// Note:	"|" denotes choice
//		"{ ... }*" denotes repetition 0+ times
//		"[ ... ]" denotes 0 or 1 times
//
//	configFile	= StmtList
//	StmtList	= { Stmt }*
//
//	Stmt		= ident_sym [ '=' | '?=' ] StringExpr ';'
//			| ident_sym [ '=' | '?=' ] ListExpr ';'
//			| ident_sym '{' StmtList '}' [ ';' ]
//			| '@include' StringExpr [ '@ifExists' ] ';'
//			| '@copyFrom' ident_sym [ '@ifExists' ] ';'
//			| '@remove' ident_sym ';'
//			| '@error' StringExpr ';'
//			| '@if' '(' Condition ')' '{' StmtList '}'
//			  { '@elseIf' '(' Condition ')' '{' StmtList '}' }*
//			  [ '@else' '{' StmtList '}' ]
//			  [ ';' ]
//
//	StringExpr	= String { '+' String }*
//
//	String		= string_sym
//			| ident_sym
//			| 'osType(' ')'
//			| 'osDirSeparator(' ')'
//			| 'osPathSeparator(' ')'
//			| 'getenv('  StringExpr [ ',' StringExpr ] ')'
//			| 'exec(' StringExpr [ ',' StringExpr ] ')'
// 			| 'join(' ListExpr ',' StringExpr ')'
// 			| 'siblingScope(' StringExpr ')'
//
//
//	ListExpr	= List { '+' List }*
//	List		= '[' StringExprList [ ',' ] ']'
//			| ident_sym
// 			| 'split(' StringExpr ',' StringExpr ')'
//
//	StringExprList = empty
//			| StringExpr { ',' StringExpr }*
//
//	Condition	= OrCondition
//	OrCondition	= AndCondition { '||' AndCondition }*
//	AndCondition	= TermCondition { '&&' TermCondition }*
//	TermCondition	= '(' Condition ')'
//			| '!' '(' Condition ')'
//			| 'isFileReadable(' StringExpr ')'
//			| StringExpr '==' StringExpr
//			| StringExpr '!=' StringExpr
//			| StringExpr '@in' ListExpr
//			| StringExpr '@matches' StringExpr
//----------------------------------------------------------------------

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

class ConfigParser {

	ConfigParser(int sourceType, String source, String trustedCmdLine, String sourceDescription, ConfigurationImpl config,
	        boolean ifExistsIsSpecified) throws ConfigurationException {
		// --------
		// Initialise instance variables
		// --------
		token = new LexToken();
		this.config = config;
		errorInIncludedFile = false;
		switch (sourceType) {
		case Configuration.INPUT_FILE:
			fileName = source;
			break;
		case Configuration.INPUT_STRING:
			if (sourceDescription.equals("")) {
				fileName = "<string-based configuration>";
			} else {
				fileName = sourceDescription;
			}
			break;
		case Configuration.INPUT_EXEC:
			if (sourceDescription.equals("")) {
				fileName = "exec#" + source;
			} else {
				fileName = sourceDescription;
			}
			source = trustedCmdLine;
			break;
		default:
			Util.assertion(false); // Bug!
			break;
		}

		// --------
		// Initialise the lexical analyser.
		// The constructor of the lexical analyser throws an exception
		// if it cannot open the specified file or execute the specified
		// command. If such an exception is thrown and if
		// "ifExistsIsSpecified" is true then we return without doing
		// any work.
		// --------
		try {
			lex = new ConfigLex(sourceType, source, this.config.uidIdentifierProcessor);
		} catch (ConfigurationException ex) {
			if (ifExistsIsSpecified) {
				return;
			} else {
				throw ex;
			}
		}

		// --------
		// Perform the actual work. Note that a config file
		// consists of a list of statements.
		// --------
		try {
			lex.nextToken(token);
			this.config.pushIncludedFilename(fileName);
			parseStmtList();
			accept(LexBase.LEX_EOF_SYM, "expecting identifier");
		} catch (ConfigurationException ex) {
			lex.destroy();
			this.config.popIncludedFilename(fileName);
			if (errorInIncludedFile) {
				throw ex;
			} else {
				throw new ConfigurationException(fileName + ", line " + token.getLineNum() + ": " + ex.getMessage());
			}
		}

		lex.destroy();
		// --------
		// Pop our file from the the stack of (include'd) files.
		// --------
		this.config.popIncludedFilename(fileName);
	}

	// --------
	// Public operations: None. All the work is done in the ctor!
	// --------

	private void parseStmtList() {
		int type;

		type = token.getType();
		while (type == LexBase.LEX_IDENT_SYM || type == ConfigLex.LEX_INCLUDE_SYM || type == ConfigLex.LEX_IF_SYM
		        || type == ConfigLex.LEX_REMOVE_SYM || type == ConfigLex.LEX_ERROR_SYM || type == ConfigLex.LEX_COPY_FROM_SYM) {
			parseStmt();
			type = token.getType();
		}
	}

	private void parseStmt() {
		LexToken identName;
		int identNameType;
		int assignmentType;

		identName = new LexToken(token); // save it
		identNameType = identName.getType();
		if (identNameType == ConfigLex.LEX_INCLUDE_SYM) {
			parseIncludeStmt();
			return;
		} else if (identNameType == ConfigLex.LEX_IF_SYM) {
			parseIfStmt();
			return;
		} else if (identNameType == ConfigLex.LEX_REMOVE_SYM) {
			parseRemoveStmt();
			return;
		} else if (identNameType == ConfigLex.LEX_ERROR_SYM) {
			parseErrorStmt();
			return;
		} else if (identNameType == ConfigLex.LEX_COPY_FROM_SYM) {
			parseCopyStmt();
			return;
		}

		if (identNameType == LexBase.LEX_IDENT_SYM && identName.getSpelling().startsWith(".")) {
			error("cannot use '.' at start of the declaration of a variable " + "or scope,");
			return;
		}
		accept(LexBase.LEX_IDENT_SYM, "expecting identifier or 'include'");

		switch (token.getType()) {
		case LexBase.LEX_QUESTION_EQUALS_SYM:
		case LexBase.LEX_EQUALS_SYM:
			assignmentType = token.getType();
			lex.nextToken(token); // consume '='
			parseRhsAssignStmt(identName, assignmentType);
			accept(LexBase.LEX_SEMICOLON_SYM, "expecting ';' or '+'");
			break;
		case LexBase.LEX_OPEN_BRACE_SYM:
			parseScope(identName);
			// --------
			// Consume an optional ";"
			// --------
			if (token.getType() == LexBase.LEX_SEMICOLON_SYM) {
				lex.nextToken(token);
			}
			break;
		default:
			error("expecting '=', '?=' or '{'"); // matching '}'
			return;
		}
	}

	private void parseIncludeStmt() {
		StringBuffer source;
		String sourceStr;
		StringBuffer trustedCmdLine;
		String execSource;
		int includeLineNum;
		boolean ifExistsIsSpecified;

		// --------
		// Consume the '@include' keyword
		// --------
		accept(ConfigLex.LEX_INCLUDE_SYM, "expecting 'include'");
		if (config.getCurrScope() != config.getRootScope()) {
			error("The '@include' command cannot be used inside a scope", false);
			return;
		}
		includeLineNum = token.getLineNum();

		// --------
		// Consume the source
		// --------
		source = new StringBuffer();
		parseStringExpr(source);
		sourceStr = source.toString();

		// --------
		// Check if this is a circular include.
		// --------
		config.checkForCircularIncludes(sourceStr, includeLineNum);

		// --------
		// Consume "@ifExists" if specified
		// --------
		if (token.getType() == ConfigLex.LEX_IF_EXISTS_SYM) {
			ifExistsIsSpecified = true;
			lex.nextToken(token);
		} else {
			ifExistsIsSpecified = false;
		}

		// --------
		// We get more intuitive error messages if we report a security
		// violation for include "exec#..." now instead of later from
		// inside a recursive call to the parser.
		// --------
		execSource = null;
		trustedCmdLine = new StringBuffer();
		if (sourceStr.startsWith("exec#")) {
			execSource = sourceStr.substring("exec#".length());
			if (!config.isExecAllowed(execSource, trustedCmdLine)) {
				throw new ConfigurationException("cannot include \"" + sourceStr + "\" due to security restrictions");
			}
		}

		// --------
		// The source is of one of the following forms:
		// "exec#<command>"
		// "file#<command>"
		// "<filename>"
		//
		// Parse the source. If there is an error then propagate it with
		// some additional text to indicate that the error was in an
		// included file.
		// --------
		try {
			if (sourceStr.startsWith("exec#")) {
				new ConfigParser(Configuration.INPUT_EXEC, execSource, trustedCmdLine.toString(), "", config, ifExistsIsSpecified);
			} else if (sourceStr.startsWith("file#")) {
				new ConfigParser(Configuration.INPUT_FILE, sourceStr.substring("file#".length()), trustedCmdLine.toString(), "", config,
				        ifExistsIsSpecified);
			} else {
				new ConfigParser(Configuration.INPUT_FILE, sourceStr, trustedCmdLine.toString(), "", config, ifExistsIsSpecified);
			}
		} catch (ConfigurationException ex) {
			errorInIncludedFile = true;
			throw new ConfigurationException(ex.getMessage() + "\n(included from " + fileName + ", line " + includeLineNum + ")");
		}

		// --------
		// Consume the terminating ';'
		// --------
		accept(LexBase.LEX_SEMICOLON_SYM, "expecting ';' or 'if exists'");
	}

	private void parseCopyStmt() {
		StringBuffer fromScopeName;
		String fromScopeNameStr;
		String toScopeName;
		String[] fromNamesArray;
		ConfigItem item;
		ConfigScope fromScope;
		String newName;
		int i;
		int fromScopeNameLen;
		boolean ifExistsIsSpecified;

		accept(ConfigLex.LEX_COPY_FROM_SYM, "expecting '@copyFrom'");
		fromScopeName = new StringBuffer();
		parseStringExpr(fromScopeName);
		fromScopeNameStr = fromScopeName.toString();
		fromScopeNameLen = fromScopeNameStr.length();

		// --------
		// Consume "@ifExists" if specified
		// --------
		if (token.getType() == ConfigLex.LEX_IF_EXISTS_SYM) {
			ifExistsIsSpecified = true;
			lex.nextToken(token);
		} else {
			ifExistsIsSpecified = false;
		}

		// --------
		// Sanity check: cannot copy from self or a parent scope
		// --------
		toScopeName = config.getCurrScope().getScopedName();
		if (toScopeName.equals(fromScopeNameStr)) {
			throw new ConfigurationException("copy statement: cannot copy " + "own scope");
		}
		if (toScopeName.startsWith(fromScopeNameStr + ".")) {
			throw new ConfigurationException("copy statement: cannot copy " + "from a parent scope");
		}

		// --------
		// If the scope does not exist and if "if exists" was specified
		// then we short-circuit the rest of this function.
		// --------
		item = config.lookup(fromScopeNameStr, fromScopeNameStr, true);
		if (item == null && ifExistsIsSpecified) {
			accept(LexBase.LEX_SEMICOLON_SYM, "expecting ';'");
			return;
		}

		if (item == null) {
			throw new ConfigurationException("copy statement: scope '" + fromScopeNameStr + "' does not exist");
		}
		if (item.getType() != Configuration.CFG_SCOPE) {
			throw new ConfigurationException("copy statement: '" + fromScopeNameStr + "' is not a scope");
		}
		fromScope = item.getScopeVal();
		Util.assertion(fromScope != null);

		// --------
		// Get a recursive listing of all the items in fromScopeName
		// --------
		fromNamesArray = fromScope.listFullyScopedNames(Configuration.CFG_SCOPE_AND_VARS, true);

		// --------
		// Copy all the items into the current scope
		// --------
		for (i = 0; i < fromNamesArray.length; i++) {
			newName = fromNamesArray[i].substring(fromScopeNameLen + 1);
			item = config.lookup(fromNamesArray[i], fromNamesArray[i], true);
			Util.assertion(item != null);
			switch (item.getType()) {
			case Configuration.CFG_STRING:
				config.insertString("", newName, item.getStringVal());
				break;
			case Configuration.CFG_LIST:
				config.insertList(newName, item.getListVal());
				break;
			case Configuration.CFG_SCOPE:
				config.ensureScopeExists(newName);
				break;
			default:
				Util.assertion(false); // Bug!
				break;
			}
		}

		// --------
		// Consume the terminating ';'
		// --------
		accept(LexBase.LEX_SEMICOLON_SYM, "expecting ';' or 'if exists'");
	}

	private void parseRemoveStmt() {
		ConfigScope currScope;
		String identName;

		accept(ConfigLex.LEX_REMOVE_SYM, "expecting 'remove'");
		identName = token.getSpelling();
		accept(LexBase.LEX_IDENT_SYM, "expecting an identifier");
		if (identName.indexOf('.') != -1) {
			throw new ConfigurationException(fileName + ": can remove entries " + "from only the current scope");
		}
		currScope = config.getCurrScope();
		if (!currScope.removeItem(identName)) {
			throw new ConfigurationException(fileName + ": '" + identName + "' does not exist in the current scope");
		}
		accept(LexBase.LEX_SEMICOLON_SYM, "expecting ';'");
	}

	private void parseErrorStmt() {
		StringBuffer msg;

		accept(ConfigLex.LEX_ERROR_SYM, "expecting 'error'");
		msg = new StringBuffer();
		parseStringExpr(msg);
		accept(LexBase.LEX_SEMICOLON_SYM, "expecting ';'");
		throw new ConfigurationException(msg.toString());
	}

	private void parseIfStmt() {
		boolean condition;
		boolean condition2;

		// --------
		// Parse the "if ( Condition ) { StmtList }" clause
		// --------
		accept(ConfigLex.LEX_IF_SYM, "expecting 'if'");
		accept(LexBase.LEX_OPEN_PAREN_SYM, "expecting '('");
		condition = parseCondition();
		accept(LexBase.LEX_CLOSE_PAREN_SYM, "expecting ')'");
		accept(LexBase.LEX_OPEN_BRACE_SYM, "expecting '{'");
		if (condition) {
			parseStmtList();
			accept(LexBase.LEX_CLOSE_BRACE_SYM, "expecting '}'");
		} else {
			skipToClosingBrace();
		}

		// --------
		// Parse 0+ "@elseIf ( Condition ) { StmtList }" clauses
		// --------
		while (token.getType() == ConfigLex.LEX_ELSE_IF_SYM) {
			lex.nextToken(token);
			accept(LexBase.LEX_OPEN_PAREN_SYM, "expecting '('");
			condition2 = parseCondition();
			accept(LexBase.LEX_CLOSE_PAREN_SYM, "expecting ')'");
			accept(LexBase.LEX_OPEN_BRACE_SYM, "expecting '{'");
			if (!condition && condition2) {
				parseStmtList();
				accept(LexBase.LEX_CLOSE_BRACE_SYM, "expecting '}'");
			} else {
				skipToClosingBrace();
			}
			condition = condition || condition2;
		}

		// --------
		// Parse the "else { StmtList }" clause, if any
		// --------
		if (token.getType() == ConfigLex.LEX_ELSE_SYM) {
			lex.nextToken(token);
			accept(LexBase.LEX_OPEN_BRACE_SYM, "expecting '{'");
			if (!condition) {
				parseStmtList();
				accept(LexBase.LEX_CLOSE_BRACE_SYM, "expecting '}'");
			} else {
				skipToClosingBrace();
			}
		}

		// --------
		// Consume an optional ";"
		// --------
		if (token.getType() == LexBase.LEX_SEMICOLON_SYM) {
			lex.nextToken(token);
		}
	}

	private void skipToClosingBrace() {
		int countOpenBraces;

		countOpenBraces = 1;
		while (countOpenBraces > 0) {
			switch (token.getType()) {
			case LexBase.LEX_OPEN_BRACE_SYM:
				countOpenBraces++;
				break;
			case LexBase.LEX_CLOSE_BRACE_SYM:
				countOpenBraces--;
				break;
			case LexBase.LEX_EOF_SYM:
				error("expecting '}'");
				break;
			default:
				break;
			}
			lex.nextToken(token);
		}
	}

	private boolean parseCondition() {
		return parseOrCondition();
	}

	private boolean parseOrCondition() {
		boolean result;
		boolean result2;

		result = parseAndCondition();
		while (token.getType() == LexBase.LEX_OR_SYM) {
			lex.nextToken(token);
			result2 = parseAndCondition();
			result = result || result2;
		}
		return result;
	}

	private boolean parseAndCondition() {
		boolean result;
		boolean result2;

		result = parseTerminalCondition();
		while (token.getType() == LexBase.LEX_AND_SYM) {
			lex.nextToken(token);
			result2 = parseTerminalCondition();
			result = result && result2;
		}
		return result;
	}

	private boolean parseTerminalCondition() {
		StringBuffer str1;
		StringBuffer str2;
		ArrayList<String> list;
		boolean result;
		int len;
		int i;

		result = false;
		if (token.getType() == LexBase.LEX_NOT_SYM) {
			lex.nextToken(token);
			accept(LexBase.LEX_OPEN_PAREN_SYM, "expecting '('");
			result = !parseCondition();
			accept(LexBase.LEX_CLOSE_PAREN_SYM, "expecting ')'");
			return result;
		}
		if (token.getType() == LexBase.LEX_OPEN_PAREN_SYM) {
			lex.nextToken(token);
			result = parseCondition();
			accept(LexBase.LEX_CLOSE_PAREN_SYM, "expecting ')'");
			return result;
		}
		if (token.getType() == ConfigLex.LEX_FUNC_IS_FILE_READABLE_SYM) {
			lex.nextToken(token);
			str1 = new StringBuffer();
			parseStringExpr(str1);
			accept(LexBase.LEX_CLOSE_PAREN_SYM, "expecting ')'");
			if (new File(str1.toString()).canRead()) {
				return true;
			} else {
				return false;
			}
		}
		str1 = new StringBuffer();
		parseStringExpr(str1);
		switch (token.getType()) {
		case LexBase.LEX_EQUALS_EQUALS_SYM:
			lex.nextToken(token);
			str2 = new StringBuffer();
			parseStringExpr(str2);
			result = str1.toString().equals(str2.toString());
			break;
		case LexBase.LEX_NOT_EQUALS_SYM:
			lex.nextToken(token);
			str2 = new StringBuffer();
			parseStringExpr(str2);
			result = !str1.toString().equals(str2.toString());
			break;
		case ConfigLex.LEX_IN_SYM:
			lex.nextToken(token);
			list = new ArrayList<String>();
			parseListExpr(list);
			len = list.size();
			result = false;
			for (i = 0; i < len; i++) {
				if (str1.toString().equals(list.get(i))) {
					result = true;
					break;
				}
			}
			break;
		case ConfigLex.LEX_MATCHES_SYM:
			lex.nextToken(token);
			str2 = new StringBuffer();
			parseStringExpr(str2);
			result = Configuration.patternMatch(str1.toString(), str2.toString());
			break;
		default:
			error("expecting '(', or a string expression");
			break;
		}
		return result;
	}

	private void parseScope(LexToken scopeName) {
		ConfigScope oldScope;
		ConfigScope newScope;

		// --------
		// Create the new scope and put it onto the stack
		// --------
		oldScope = config.getCurrScope();
		newScope = config.ensureScopeExists(scopeName.getSpelling());
		config.setCurrScope(newScope);

		// --------
		// Do the actual parsing
		// --------
		accept(LexBase.LEX_OPEN_BRACE_SYM, "expecting '{'");
		parseStmtList();
		accept(LexBase.LEX_CLOSE_BRACE_SYM, "expecting an identifier or '}'");

		// --------
		// Finally, pop the scope from the stack
		// --------
		config.setCurrScope(oldScope);
	}

	private void parseRhsAssignStmt(LexToken varName, int assignmentType) {
		StringBuffer stringExpr;
		ArrayList<String> listExpr;
		int varType;
		boolean doAssign;

		switch (config.type(varName.getSpelling(), "")) {
		case Configuration.CFG_STRING:
		case Configuration.CFG_LIST:
			if (assignmentType == LexBase.LEX_QUESTION_EQUALS_SYM) {
				doAssign = false;
			} else {
				doAssign = true;
			}
			break;
		default:
			doAssign = true;
		}

		// --------
		// Examine the current token to determine whether the
		// expression to be parsed is a stringExpr or an listExpr.
		// --------
		switch (token.getType()) {
		case LexBase.LEX_OPEN_BRACKET_SYM:
		case ConfigLex.LEX_FUNC_SPLIT_SYM:
			varType = Configuration.CFG_LIST;
			break;
		case ConfigLex.LEX_FUNC_GETENV_SYM:
		case ConfigLex.LEX_FUNC_EXEC_SYM:
		case ConfigLex.LEX_FUNC_JOIN_SYM:
		case ConfigLex.LEX_FUNC_READ_FILE_SYM:
		case ConfigLex.LEX_FUNC_REPLACE_SYM:
		case ConfigLex.LEX_FUNC_OS_TYPE_SYM:
		case ConfigLex.LEX_FUNC_OS_DIR_SEP_SYM:
		case ConfigLex.LEX_FUNC_OS_PATH_SEP_SYM:
		case ConfigLex.LEX_FUNC_FILE_TO_DIR_SYM:
		case ConfigLex.LEX_FUNC_CONFIG_FILE_SYM:
		case ConfigLex.LEX_FUNC_CONFIG_TYPE_SYM:
		case ConfigLex.LEX_FUNC_SIBLING_SCOPE_SYM:
		case LexBase.LEX_STRING_SYM:
			varType = Configuration.CFG_STRING;
			break;
		case LexBase.LEX_IDENT_SYM:
			// --------
			// This identifier (hopefully) denotes an already
			// existing variable. We have to determine the type
			// of the variable (it is either a string or a list)
			// in order to proceed with the parsing.
			// --------
			switch (config.type(token.getSpelling(), "")) {
			case Configuration.CFG_STRING:
				varType = Configuration.CFG_STRING;
				break;
			case Configuration.CFG_LIST:
				varType = Configuration.CFG_LIST;
				break;
			default:
				error("identifier '" + token.getSpelling() + "' not previously declared", false);
				return;
			}
			break;
		default:
			error("expecting a string, identifier or '['");
			// matching ']'
			return;
		}

		// --------
		// Now that we know the type of the input expression, we
		// can parse it correctly.
		// --------
		switch (varType) {
		case Configuration.CFG_STRING:
			stringExpr = new StringBuffer();
			parseStringExpr(stringExpr);
			if (doAssign) {
				config.insertString("", varName.getSpelling(), stringExpr.toString());
			}
			break;
		case Configuration.CFG_LIST:
			listExpr = new ArrayList<String>();
			parseListExpr(listExpr);
			if (doAssign) {
				config.insertList(varName.getSpelling(), listExpr);
			}
			break;
		default:
			Util.assertion(false); // Bug
			break;
		}
	}

	private void parseStringExpr(StringBuffer expr) {
		StringBuffer expr2;

		parseString(expr);
		while (token.getType() == LexBase.LEX_PLUS_SYM) {
			lex.nextToken(token); // consume '+'
			expr2 = new StringBuffer();
			parseString(expr2);
			expr.append(expr2);
		}
	}

	private void parseString(StringBuffer str) {
		int type;
		StringBuffer name;
		ConfigItem item;

		str.delete(0, str.length());
		switch (token.getType()) {
		case ConfigLex.LEX_FUNC_SIBLING_SCOPE_SYM:
			parseSiblingScope(str);
			break;
		case ConfigLex.LEX_FUNC_GETENV_SYM:
			parseEnv(str);
			break;
		case ConfigLex.LEX_FUNC_EXEC_SYM:
			parseExec(str);
			break;
		case ConfigLex.LEX_FUNC_JOIN_SYM:
			parseJoin(str);
			break;
		case ConfigLex.LEX_FUNC_READ_FILE_SYM:
			parseReadFile(str);
			break;
		case ConfigLex.LEX_FUNC_REPLACE_SYM:
			parseReplace(str);
			break;
		case ConfigLex.LEX_FUNC_OS_TYPE_SYM:
			if (System.getProperty("file.separator").equals("\\")) {
				str.append("windows");
			} else {
				str.append("unix");
			}
			lex.nextToken(token);
			accept(LexBase.LEX_CLOSE_PAREN_SYM, "expecting ')'");
			break;
		case ConfigLex.LEX_FUNC_OS_DIR_SEP_SYM:
			str.append(System.getProperty("file.separator"));
			lex.nextToken(token);
			accept(LexBase.LEX_CLOSE_PAREN_SYM, "expecting ')'");
			break;
		case ConfigLex.LEX_FUNC_OS_PATH_SEP_SYM:
			str.append(System.getProperty("path.separator"));
			lex.nextToken(token);
			accept(LexBase.LEX_CLOSE_PAREN_SYM, "expecting ')'");
			break;
		case ConfigLex.LEX_FUNC_FILE_TO_DIR_SYM:
			lex.nextToken(token);
			name = new StringBuffer();
			parseStringExpr(name);
			accept(LexBase.LEX_CLOSE_PAREN_SYM, "expecting ')'");
			str.append(getDirectoryOfFile(name.toString()));
			break;
		case ConfigLex.LEX_FUNC_CONFIG_FILE_SYM:
			lex.nextToken(token);
			accept(LexBase.LEX_CLOSE_PAREN_SYM, "expecting ')'");
			str.append(fileName);
			break;
		case ConfigLex.LEX_FUNC_CONFIG_TYPE_SYM:
			lex.nextToken(token);
			name = new StringBuffer();
			parseStringExpr(name);
			accept(LexBase.LEX_CLOSE_PAREN_SYM, "expecting ')'");
			item = config.lookup(name.toString(), name.toString());
			if (item == null) {
				type = Configuration.CFG_NO_VALUE;
			} else {
				type = item.getType();
			}
			switch (type) {
			case Configuration.CFG_STRING:
				str.append("string");
				break;
			case Configuration.CFG_LIST:
				str.append("list");
				break;
			case Configuration.CFG_SCOPE:
				str.append("scope");
				break;
			case Configuration.CFG_NO_VALUE:
				str.append("no_value");
				break;
			default:
				Util.assertion(false); // Bug!
				break;
			}
			break;
		case LexBase.LEX_STRING_SYM:
			str.append(token.getSpelling());
			lex.nextToken(token);
			break;
		case LexBase.LEX_IDENT_SYM:
			type = config.stringValueAndType(token.getSpelling(), token.getSpelling(), str);
			switch (type) {
			case Configuration.CFG_STRING:
				break;
			case Configuration.CFG_NO_VALUE:
				error("identifier '" + token.getSpelling() + "' not previously declared", false);
				return;
			case Configuration.CFG_SCOPE:
				error("identifier '" + token.getSpelling() + "' is a scope instead of a string", false);
				return;
			case Configuration.CFG_LIST:
				error("identifier '" + token.getSpelling() + "' is a list instead of a string", false);
				return;
			default:
				Util.assertion(false); // Bug
				return;
			}
			lex.nextToken(token);
			break;
		default:
			error("expecting a string or identifier");
			return;
		}
	}

	private void parseExec(StringBuffer str) {
		StringBuffer cmd;
		boolean hasDefaultStr;
		StringBuffer defaultStr;
		boolean execStatus;
		StringBuffer trustedCmdLine;

		str.delete(0, str.length());
		// --------
		// Parse the command and default value, if any
		// --------
		accept(ConfigLex.LEX_FUNC_EXEC_SYM, "expecting 'os.exec('");
		cmd = new StringBuffer();
		parseStringExpr(cmd);
		defaultStr = new StringBuffer();
		if (token.getType() == LexBase.LEX_COMMA_SYM) {
			accept(LexBase.LEX_COMMA_SYM, "expecting ','");
			parseStringExpr(defaultStr);
			hasDefaultStr = true;
		} else {
			hasDefaultStr = false;
		}

		trustedCmdLine = new StringBuffer();
		if (!config.isExecAllowed(cmd.toString(), trustedCmdLine)) {
			throw new ConfigurationException("cannot execute \"" + cmd + "\" due to security restrictions");
		}

		// --------
		// Execute the command and decide if we throw an exception,
		// return the default value, if any, or return the output of
		// the successful execCmd().
		// --------
		execStatus = Util.execCmd(trustedCmdLine.toString(), str);
		if (!execStatus && !hasDefaultStr) {
			throw new ConfigurationException("os.exec(\"" + cmd + "\") failed: " + str);
		} else if (!execStatus && hasDefaultStr) {
			str.append(defaultStr);
		} else {
			Util.assertion(execStatus == true);
		}

		accept(LexBase.LEX_CLOSE_PAREN_SYM, "expecting ')'");
	}

	private void parseJoin(StringBuffer str) {
		ArrayList<String> list;
		StringBuffer separator;
		int len;
		int i;

		str.delete(0, str.length());
		accept(ConfigLex.LEX_FUNC_JOIN_SYM, "expecting 'join('");
		list = new ArrayList<String>();
		parseListExpr(list);
		accept(LexBase.LEX_COMMA_SYM, "expecting ','");
		separator = new StringBuffer();
		parseStringExpr(separator);
		accept(LexBase.LEX_CLOSE_PAREN_SYM, "expecting ')'");

		len = list.size();
		for (i = 0; i < len; i++) {
			str.append(list.get(i));
			if (i < len - 1) {
				str.append(separator.toString());
			}
		}
	}

	private void parseReplace(StringBuffer result) {
		StringBuffer origBuf;
		StringBuffer searchBuf;
		StringBuffer replacementBuf;
		String origStr;
		String searchStr;
		String replacementStr;
		int searchStrLen;
		int currStart;
		int pIndex;

		origBuf = new StringBuffer();
		searchBuf = new StringBuffer();
		replacementBuf = new StringBuffer();
		accept(ConfigLex.LEX_FUNC_REPLACE_SYM, "expecting 'replace('");
		parseStringExpr(origBuf);
		accept(LexBase.LEX_COMMA_SYM, "expecting ','");
		parseStringExpr(searchBuf);
		accept(LexBase.LEX_COMMA_SYM, "expecting ','");
		parseStringExpr(replacementBuf);
		accept(LexBase.LEX_CLOSE_PAREN_SYM, "expecting ')'");

		result.delete(0, result.length());
		origStr = origBuf.toString();
		searchStr = searchBuf.toString();
		replacementStr = replacementBuf.toString();
		searchStrLen = searchStr.length();
		currStart = 0;
		pIndex = origStr.indexOf(searchStr, currStart);
		while (pIndex != -1) {
			result.append(origStr.substring(currStart, pIndex));
			result.append(replacementStr);
			currStart = pIndex + searchStrLen;
			pIndex = origStr.indexOf(searchStr, currStart);
		}
		result.append(origStr.substring(currStart));
	}

	private void parseSplit(ArrayList<String> result) {
		StringBuffer buf;
		StringBuffer delim;
		String bufStr;
		String delimStr;
		int delimLen;
		int currStart;
		int pIndex;

		buf = new StringBuffer();
		delim = new StringBuffer();
		accept(ConfigLex.LEX_FUNC_SPLIT_SYM, "expecting 'split('");
		parseStringExpr(buf);
		accept(LexBase.LEX_COMMA_SYM, "expecting ','");
		parseStringExpr(delim);
		accept(LexBase.LEX_CLOSE_PAREN_SYM, "expecting ')'");

		bufStr = buf.toString();
		delimStr = delim.toString();
		delimLen = delimStr.length();
		currStart = 0;
		pIndex = bufStr.indexOf(delimStr, currStart);
		while (pIndex != -1) {
			result.add(bufStr.substring(currStart, pIndex));
			currStart = pIndex + delimLen;
			pIndex = bufStr.indexOf(delimStr, currStart);
		}
		result.add(bufStr.substring(currStart));
	}

	private void parseListExpr(ArrayList<String> expr) {
		ArrayList<String> expr2;

		expr2 = new ArrayList<String>();
		expr.clear();
		parseList(expr);
		while (token.getType() == LexBase.LEX_PLUS_SYM) {
			lex.nextToken(token); // consume '+'
			expr2.clear();
			parseList(expr2);
			expr.addAll(expr2);
		}
	}

	private void parseList(ArrayList<String> expr) {
		int type;

		switch (token.getType()) {
		case ConfigLex.LEX_FUNC_SPLIT_SYM:
			parseSplit(expr);
			break;
		case LexBase.LEX_OPEN_BRACKET_SYM:
			// --------
			// '[' StringExprList ']'
			// --------
			lex.nextToken(token); // consume '['
			parseStringExprList(expr);
			accept(LexBase.LEX_CLOSE_BRACKET_SYM, "expecting ']'");
			break;
		case LexBase.LEX_IDENT_SYM:
			// --------
			// ident_sym: make sure the identifier is a list
			// --------
			type = config.listValueAndType(token.getSpelling(), token.getSpelling(), expr);
			if (type != Configuration.CFG_LIST) {
				error("identifier '" + token.getSpelling() + "' is not a list", false);
			}
			lex.nextToken(token); // consume the identifier
			break;
		default:
			error("expecting an identifier or '['"); // matching ']'
			break;
		}
	}

	private void parseStringExprList(ArrayList<String> list) {
		StringBuffer str;
		int type;

		list.clear();
		type = token.getType();
		if (type == LexBase.LEX_CLOSE_BRACKET_SYM) {
			return; // empty list
		}
		if (!token.isStringFunc() && type != LexBase.LEX_STRING_SYM && type != LexBase.LEX_IDENT_SYM) {
			error("expecting a string or ']'");
		}

		str = new StringBuffer();
		parseStringExpr(str);
		list.add(str.toString());
		while (token.getType() == LexBase.LEX_COMMA_SYM) {
			lex.nextToken(token);
			if (token.getType() == LexBase.LEX_CLOSE_BRACKET_SYM) {
				return;
			}
			parseStringExpr(str);
			list.add(str.toString());
		}
	}

	private String getDirectoryOfFile(String file) {
		int len;
		int i;
		int j;
		boolean found;
		char osSep;
		char ch;
		StringBuffer result;

		result = new StringBuffer();
		osSep = System.getProperty("file.separator").charAt(0);
		len = file.length();
		found = false;
		for (i = len - 1; i >= 0; i--) {
			ch = file.charAt(i);
			if (ch == '/' || ch == osSep) {
				found = true;
				break;
			}
		}
		if (!found) {
			// --------
			// Case 1. "foo.cfg" -> "." (UNIX & Windows)
			// --------
			result.append(".");
		} else if (i == 0) {
			// --------
			// Case 2. "/foo.cfg" -> "/." (UNIX & Windows)
			// Or: "\foo.cfg" -> "\." (Windows only)
			// --------
			result.append(file.charAt(0)).append(".");
		} else {
			// --------
			// Case 3. "/tmp/foo.cfg" -> "/tmp" (UNIX & Windows)
			// Or: "C:\foo.cfg" -> "C:\." (Windows only)
			// --------
			Util.assertion(i > 0);
			for (j = 0; j < i; j++) {
				result.append(file.charAt(j));
			}
			ch = file.charAt(0);
			if (i == 2 && (Character.isUpperCase(ch) || Character.isLowerCase(ch)) && file.charAt(1) == ':') {
				result.append(file.charAt(i)).append(".");
			}
		}
		return result.toString();
	}

	private void accept(int symbolType, String errMsg) {
		if (token.getType() == symbolType) {
			lex.nextToken(token);
		} else {
			error(errMsg);
		}
	}

	private void error(String errMsg) {
		error(errMsg, true);
	}

	private void error(String errMsg, boolean printNear) {
		int type;

		// --------
		// In order to provide good error messages, lexical errors
		// take precedence over parsing errors. For example, there
		// is no point in printing out "was expecting a string or
		// identifier" if the real problem is that the lexical
		// analyser returned a string_with_eol_sym symbol.
		// --------
		type = token.getType();
		switch (type) {
		case LexBase.LEX_UNKNOWN_FUNC_SYM:
			throw new ConfigurationException("'" + token.getSpelling() + "' is not a built-in function");
		case LexBase.LEX_SOLE_DOT_IDENT_SYM:
			throw new ConfigurationException("'.' is not a valid identifier");
		case LexBase.LEX_TWO_DOTS_IDENT_SYM:
			throw new ConfigurationException("'..' appears in identified '" + token.getSpelling() + "'");
		case LexBase.LEX_STRING_WITH_EOL_SYM:
			throw new ConfigurationException("end-of-line not allowed in " + "string '" + token.getSpelling() + "'");
		case LexBase.LEX_BLOCK_STRING_WITH_EOF_SYM:
			throw new ConfigurationException("end-of-file encountered in " + "block string starting at line " + token.getLineNum());
		case LexBase.LEX_ILLEGAL_IDENT_SYM:
			throw new ConfigurationException("'" + token.getSpelling() + "' is not a legal identifier");
		default:
			// No lexical error. Handle the parsing error below.
			break;
		}

		// --------
		// If we get this far then it means that we have to report
		// a parsing error (as opposed to a lexical error; they have
		// already been handled).
		// --------

		if (printNear && type == LexBase.LEX_STRING_SYM) {
			throw new ConfigurationException(errMsg + " near \"" + token.getSpelling() + "\"");
		} else if (printNear && type != LexBase.LEX_STRING_SYM) {
			throw new ConfigurationException(errMsg + " near '" + token.getSpelling() + "'");
		} else {
			throw new ConfigurationException(errMsg);
		}
	}

	private void parseEnv(StringBuffer str) {
		StringBuffer envVarName;
		StringBuffer defaultStr;
		boolean hasDefaultStr;
		String val;

		envVarName = new StringBuffer();
		defaultStr = new StringBuffer();
		accept(ConfigLex.LEX_FUNC_GETENV_SYM, "expecting 'os.env('");
		parseStringExpr(envVarName);
		if (token.getType() == LexBase.LEX_COMMA_SYM) {
			accept(LexBase.LEX_COMMA_SYM, "expecting ','");
			parseStringExpr(defaultStr);
			hasDefaultStr = true;
		} else {
			hasDefaultStr = false;
		}
		accept(LexBase.LEX_CLOSE_PAREN_SYM, "expecting ')'");
		val = config.getenv(envVarName.toString());
		if (val == null && hasDefaultStr) {
			val = defaultStr.toString();
		}
		if (val == null) {
			throw new ConfigurationException("cannot access the '" + envVarName + "' environment variable");
		}
		str.delete(0, str.length());
		str.append(val);
	}

	private void parseSiblingScope(StringBuffer str) {
		StringBuffer siblingName;
		String val;
		ConfigScope currScope;
		String parentScopeName;

		siblingName = new StringBuffer();
		accept(ConfigLex.LEX_FUNC_SIBLING_SCOPE_SYM, "expecting 'siblingScope('");
		currScope = config.getCurrScope();
		if (currScope == config.getRootScope()) {
			error("The siblingScope() function cannot be used in the " + "root scope", false);
			return;
		}
		parentScopeName = currScope.getParentScope().getScopedName();
		parseStringExpr(siblingName);
		accept(LexBase.LEX_CLOSE_PAREN_SYM, "expecting ')'");
		val = Configuration.mergeNames(parentScopeName, siblingName.toString());
		str.delete(0, str.length());
		str.append(val);
	}

	private void parseReadFile(StringBuffer str) {
		StringBuffer fileName;
		int ch;
		BufferedReader br;

		accept(ConfigLex.LEX_FUNC_READ_FILE_SYM, "expecting 'file.read('");
		fileName = new StringBuffer();
		parseStringExpr(fileName);
		accept(LexBase.LEX_CLOSE_PAREN_SYM, "expecting ')'");
		str.delete(0, str.length());
		br = null;
		try {
			br = new BufferedReader(new FileReader(fileName.toString()));
			while ((ch = br.read()) != -1) {
				if (ch == '\r') {
					continue;
				}
				str.append((char) ch);
			}
			br.close();
		} catch (IOException ex) {
			if (br != null) {
				try {
					br.close();
				} catch (IOException ex2) {
				}
			}
			throw new ConfigurationException("error reading " + ex.getMessage());
		}
	}

	// --------
	// Instance variables
	// --------
	private ConfigLex lex;
	private LexToken token;
	private ConfigurationImpl config;
	private boolean errorInIncludedFile;
	private String fileName;
}
