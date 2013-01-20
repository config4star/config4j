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

class ConfigLex extends LexBase {
	// --------
	// Constants for lexical symbols.
	// First, keywords
	// --------
	final static short LEX_COPY_FROM_SYM = 101;
	final static short LEX_ELSE_IF_SYM = 103;
	final static short LEX_ELSE_SYM = 102;
	final static short LEX_ERROR_SYM = 104;
	// --------
	// Now, functions
	// --------
	final static short LEX_FUNC_CONFIG_FILE_SYM = 201;
	final static short LEX_FUNC_CONFIG_TYPE_SYM = 202;
	final static short LEX_FUNC_EXEC_SYM = 203;
	final static short LEX_FUNC_FILE_TO_DIR_SYM = 204;
	final static short LEX_FUNC_GETENV_SYM = 205;
	final static short LEX_FUNC_IS_FILE_READABLE_SYM = 206;
	final static short LEX_FUNC_JOIN_SYM = 207;
	final static short LEX_FUNC_OS_DIR_SEP_SYM = 208;
	final static short LEX_FUNC_OS_PATH_SEP_SYM = 209;
	final static short LEX_FUNC_OS_TYPE_SYM = 210;
	final static short LEX_FUNC_READ_FILE_SYM = 211;
	final static short LEX_FUNC_REPLACE_SYM = 212;
	final static short LEX_FUNC_SIBLING_SCOPE_SYM = 213;
	final static short LEX_FUNC_SPLIT_SYM = 214;
	final static short LEX_IF_EXISTS_SYM = 106;
	final static short LEX_IF_SYM = 105;
	final static short LEX_IN_SYM = 107;
	final static short LEX_INCLUDE_SYM = 108;
	final static short LEX_MATCHES_SYM = 109;
	final static short LEX_REMOVE_SYM = 110;

	private LexFuncInfo[] configFuncInfoArray = new LexFuncInfo[] { new LexFuncInfo("configFile(", STRING_FUNC, LEX_FUNC_CONFIG_FILE_SYM),
	        new LexFuncInfo("configType(", STRING_FUNC, LEX_FUNC_CONFIG_TYPE_SYM),
	        new LexFuncInfo("exec(", STRING_FUNC, LEX_FUNC_EXEC_SYM), new LexFuncInfo("fileToDir(", STRING_FUNC, LEX_FUNC_FILE_TO_DIR_SYM),
	        new LexFuncInfo("getenv(", STRING_FUNC, LEX_FUNC_GETENV_SYM),
	        new LexFuncInfo("isFileReadable(", BOOL_FUNC, LEX_FUNC_IS_FILE_READABLE_SYM),
	        new LexFuncInfo("join(", STRING_FUNC, LEX_FUNC_JOIN_SYM),
	        new LexFuncInfo("osDirSeparator(", STRING_FUNC, LEX_FUNC_OS_DIR_SEP_SYM),
	        new LexFuncInfo("osPathSeparator(", STRING_FUNC, LEX_FUNC_OS_PATH_SEP_SYM),
	        new LexFuncInfo("osType(", STRING_FUNC, LEX_FUNC_OS_TYPE_SYM),
	        new LexFuncInfo("readFile(", STRING_FUNC, LEX_FUNC_READ_FILE_SYM),
	        new LexFuncInfo("replace(", STRING_FUNC, LEX_FUNC_REPLACE_SYM),
	        new LexFuncInfo("siblingScope(", STRING_FUNC, LEX_FUNC_SIBLING_SCOPE_SYM),
	        new LexFuncInfo("split(", LIST_FUNC, LEX_FUNC_SPLIT_SYM), };

	// --------
	// Instance variables
	// --------
	private LexKeywordInfo[] configKeywordInfoArray = new LexKeywordInfo[] { new LexKeywordInfo("@copyFrom", LEX_COPY_FROM_SYM),
	        new LexKeywordInfo("@else", LEX_ELSE_SYM), new LexKeywordInfo("@elseIf", LEX_ELSE_IF_SYM),
	        new LexKeywordInfo("@error", LEX_ERROR_SYM), new LexKeywordInfo("@if", LEX_IF_SYM),
	        new LexKeywordInfo("@ifExists", LEX_IF_EXISTS_SYM), new LexKeywordInfo("@in", LEX_IN_SYM),
	        new LexKeywordInfo("@include", LEX_INCLUDE_SYM), new LexKeywordInfo("@matches", LEX_MATCHES_SYM),
	        new LexKeywordInfo("@remove", LEX_REMOVE_SYM), };

	ConfigLex(int sourceType, String source, UidIdentifierProcessor uidIdentifierProcessor) throws ConfigurationException {
		super(sourceType, source, uidIdentifierProcessor);
		keywordInfoArray = configKeywordInfoArray;
		funcInfoArray = configFuncInfoArray;
	}
}
