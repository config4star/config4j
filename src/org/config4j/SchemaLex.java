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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;


class SchemaLex extends LexBase
{
	//--------
	// Constants for lexical symbols.
	// Keywords
	//--------
	final static short LEX_IGNORE_EVERYTHING_IN_SYM  = 101;
	final static short LEX_IGNORE_SCOPES_IN_SYM      = 102;
	final static short LEX_IGNORE_VARIABLES_IN_SYM   = 103;
	final static short LEX_TYPEDEF_SYM               = 104;
	final static short LEX_OPTIONAL_SYM              = 105;
	final static short LEX_REQUIRED_SYM              = 106;
	//--------
	// There are no functions in the schema language
	//--------

	SchemaLex(String str) throws ConfigurationException
	{
		super(str);
		this.keywordInfoArray = schemaKeywordInfoArray;
	}


	//--------
	// Instance variables
	//--------
	private LexKeywordInfo[] schemaKeywordInfoArray = new LexKeywordInfo[] {
		new LexKeywordInfo("@ignoreEverythingIn", LEX_IGNORE_EVERYTHING_IN_SYM),
		new LexKeywordInfo("@ignoreScopesIn",     LEX_IGNORE_SCOPES_IN_SYM),
		new LexKeywordInfo("@ignoreVariablesIn",  LEX_IGNORE_VARIABLES_IN_SYM),
		new LexKeywordInfo("@optional",           LEX_OPTIONAL_SYM),
		new LexKeywordInfo("@required",           LEX_REQUIRED_SYM),
		new LexKeywordInfo("@typedef",            LEX_TYPEDEF_SYM),
	};
}
