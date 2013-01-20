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


public class LexBase
{
	//--------
	// Constants for the type of a function.
	//--------
	public final static short NOT_A_FUNC  = 1;
	public final static short STRING_FUNC = 2;
	public final static short LIST_FUNC   = 3;
	public final static short BOOL_FUNC   = 4;

	//--------
	// Constants for lexical symbols, for everything except
	// keywords and function names. Constants for those are
	// defined in a subclass.
	//--------
	public final static short LEX_IDENT_SYM                 =  1;
	public final static short LEX_SEMICOLON_SYM             =  2;
	public final static short LEX_PLUS_SYM                  =  3;
	public final static short LEX_QUESTION_EQUALS_SYM       =  4;
	public final static short LEX_EQUALS_SYM                =  5;
	public final static short LEX_EQUALS_EQUALS_SYM         =  6;
	public final static short LEX_NOT_EQUALS_SYM            =  7;
	public final static short LEX_STRING_SYM                =  8;
	public final static short LEX_COMMA_SYM                 =  9;
	public final static short LEX_AND_SYM                   = 10;
	public final static short LEX_OR_SYM                    = 11;
	public final static short LEX_NOT_SYM                   = 12;
	public final static short LEX_AT_SYM                    = 13;
	public final static short LEX_OPEN_BRACKET_SYM          = 14;
	public final static short LEX_CLOSE_BRACKET_SYM         = 15;
	public final static short LEX_OPEN_BRACE_SYM            = 16;
	public final static short LEX_CLOSE_BRACE_SYM           = 17;
	public final static short LEX_OPEN_PAREN_SYM            = 18;
	public final static short LEX_CLOSE_PAREN_SYM           = 19;
	public final static short LEX_EOF_SYM                   = 20;
	public final static short LEX_UNKNOWN_SYM               = 21;
	public final static short LEX_UNKNOWN_FUNC_SYM          = 22;
	public final static short LEX_IDENT_TOO_LONG_SYM        = 23;
	public final static short LEX_STRING_WITH_EOL_SYM       = 24;
	public final static short LEX_ILLEGAL_IDENT_SYM         = 25;
	public final static short LEX_TWO_DOTS_IDENT_SYM        = 26;
	public final static short LEX_BLOCK_STRING_WITH_EOF_SYM = 27;
	public final static short LEX_SOLE_DOT_IDENT_SYM        = 28;


	public void destroy()
	{
		if (file != null) {
			try {
				file.close();
			} catch(Exception ex) {
			}
		}
		file = null;
	}


	public void nextToken(LexToken token)
	{
		StringBuffer		spelling;
		short				symbol;
		int					lineNum;
		int					i;

		Util.assertion(token != null);
		//--------
		// Skip leading white space
		//--------
		while (ch != EOF && Character.isWhitespace((char)ch)) {
			nextChar();
		}

		//--------
		// Note the line number at the start of the token
		//--------
		lineNum = this.lineNum;

		//--------
		// Check for EOF.
		//--------
		if (ch == EOF) {
			if (sourceType == Configuration.INPUT_STRING) {
				token.reset(LEX_EOF_SYM, this.lineNum, "<end of string>");
			} else {
				token.reset(LEX_EOF_SYM, this.lineNum, "<end of file>");
			}
			return;
		}

		//--------
		// Miscellaneous kinds of tokens.
		//--------
		spelling = new StringBuffer();
		switch ((char)ch) {
		case '?':
			nextChar();
			if (ch == '=') {
				nextChar();
				token.reset(LEX_QUESTION_EQUALS_SYM, lineNum, "!=");
			} else {
				token.reset(LEX_UNKNOWN_SYM, lineNum, spelling.toString());
			}
			return;
		case '!':
			nextChar();
			if (ch == '=') {
				nextChar();
				token.reset(LEX_NOT_EQUALS_SYM, lineNum, "!=");
			} else {
				token.reset(LEX_NOT_SYM, lineNum, "!");
			}
			return;
		case '@':
			spelling.append('@');
			nextChar();
			while (ch != EOF && isKeywordChar((char)ch)) {
				spelling.append((char)ch);
				nextChar();
			}
			i = searchForKeyword(spelling.toString());
			if (i >= 0) {
				token.reset(keywordInfoArray[i].symbol, lineNum,
							spelling.toString());
			} else {
				token.reset(LEX_UNKNOWN_SYM, lineNum, spelling.toString());
			}
			return;
		case '+':
			nextChar();
			token.reset(LEX_PLUS_SYM, lineNum, "+");
			return;
		case '&':
			nextChar();
			if (ch == '&') {
				nextChar();
				token.reset(LEX_AND_SYM, lineNum,"&&");
			} else {
				spelling.append('&').append((char)ch);
				token.reset(LEX_UNKNOWN_SYM, lineNum, spelling.toString());
			}
			return;
		case '|':
			nextChar();
			if (ch == '|') {
				nextChar();
				token.reset(LEX_OR_SYM, lineNum, "||");
			} else {
				spelling.append('|').append((char)ch);
				token.reset(LEX_UNKNOWN_SYM, lineNum, spelling.toString());
			}
			return;
		case '=':
			nextChar();
			if (ch == '=') {
				nextChar();
				token.reset(LEX_EQUALS_EQUALS_SYM, lineNum, "==");
			} else {
				token.reset(LEX_EQUALS_SYM, lineNum, "=");
			}
			return;
		case ';':
			nextChar();
			token.reset(LEX_SEMICOLON_SYM, lineNum, ";");
			return;
		case '[':
			nextChar();
			token.reset(LEX_OPEN_BRACKET_SYM, lineNum, "[");
			return;
		case ']':
			nextChar();
			token.reset(LEX_CLOSE_BRACKET_SYM, lineNum, "]");
			return;
		case '{':
			nextChar();
			token.reset(LEX_OPEN_BRACE_SYM, lineNum, "{");
			return;
		case '}':
			nextChar();
			token.reset(LEX_CLOSE_BRACE_SYM, lineNum, "}");
			return;
		case '(':
			nextChar();
			token.reset(LEX_OPEN_PAREN_SYM, lineNum, "(");
			return;
		case ')':
			nextChar();
			token.reset(LEX_CLOSE_PAREN_SYM, lineNum, ")");
			return;
		case ',':
			nextChar();
			token.reset(LEX_COMMA_SYM, lineNum, ",");
			return;
		case '"':
			consumeString(token);
			return;
		case '<':
			nextChar();
			if (ch != '%') {
				token.reset(LEX_UNKNOWN_SYM, lineNum, "<");
				return;
			}
			nextChar(); // skip over '%'
			consumeBlockString(token);
			return;
		case '#':
			//--------
			// A comment. Consume it and immediately following
			// comments (without resorting to recursion).
			//--------
			while (ch == '#') {
				//--------
				// Skip to the end of line
				//--------
				while (ch != EOF && ch != '\n') {
					nextChar();
				}
				if (ch == '\n') {
					nextChar();
				}
				//--------
				// Skip leading white space on the next line
				//--------
				while (ch != EOF && Character.isWhitespace(
							(char)ch))
				{
					nextChar();
				}
				//--------
				// Potentially loop around again to consume
				// more comment lines that follow immediately.
				//--------
			}
			//--------
			// Now use (a guaranteed single level of) recursion
			// to obtain the next (non-comment) token.
			//--------
			nextToken(token);
			return;
		}

		//--------
		// Is it a function or identifier?
		//--------
		if (isIdentifierChar((char)ch)) {
			//--------
			// Consume all the identifier characters
			// but not an immediately following "(", if any
			//--------
			spelling.append((char)ch);
			nextChar();
			while (ch != EOF && isIdentifierChar((char)ch)) {
				spelling.append((char)ch);
				nextChar();
			}

			//--------
			// If "(" follows immediately then it is (supposed to be)
			// a function.
			//--------
			if (ch == '(') {
				spelling.append((char)ch);
				nextChar();
				i = searchForFunction(spelling.toString());
				if (i != -1) {
					symbol = funcInfoArray[i].symbol;
					token.reset(symbol, lineNum, spelling.toString(),
						funcInfoArray[i].funcType);
				} else {
					token.reset(LEX_UNKNOWN_FUNC_SYM, lineNum,
								spelling.toString());
				}
				return;
			}

			//--------
			// It's not a function. Looks like an identifier.
			// Better check it's a legal identifier.
			//--------
			else if (spelling.toString().equals(".")) {
				token.reset(LEX_SOLE_DOT_IDENT_SYM,
						lineNum, spelling.toString());
			} else if (spelling.toString().indexOf("..") != -1) {
				token.reset(LEX_TWO_DOTS_IDENT_SYM,
						lineNum, spelling.toString());
			} else {
				try {
					spelling = uidIdentifierProcessor.expand(spelling);
					token.reset(LEX_IDENT_SYM, lineNum, spelling.toString());
				} catch (ConfigurationException ex) {
					token.reset(LEX_ILLEGAL_IDENT_SYM, lineNum,
								spelling.toString());
				}
			}
			return;
		}

		//--------
		// None of the above
		//--------
		spelling.append((char)ch);
		nextChar();
		token.reset(LEX_UNKNOWN_SYM, lineNum,
				spelling.toString());
	}


	private static final int EOF = -1;


	protected LexBase(
		int							sourceType,
		String						source,
		UidIdentifierProcessor		uidIdentifierProcessor)
												throws ConfigurationException
	{
		Util.assertion(uidIdentifierProcessor != null);
		this.uidIdentifierProcessor = uidIdentifierProcessor;
		this.sourceType = sourceType;
		this.source = source;
		this.lineNum = 1;
		this.ptr = null;
		this.ptrIndex = 0;
		this.ptrLen = 0;
		this.execOutput = null;

		this.keywordInfoArray = null;
		this.funcInfoArray = null;

		switch (sourceType) {
		case Configuration.INPUT_FILE:
			try {
				file = new BufferedReader(new FileReader(source));
			} catch (Exception ex) {
				throw new ConfigurationException("cannot open '" + source
												+ "': " + ex.getMessage());
			}
			break;
		case Configuration.INPUT_STRING:
			this.ptr = source;
			this.ptrIndex = 0;
			this.ptrLen = this.ptr.length();
			break;
		case Configuration.INPUT_EXEC:
			this.execOutput = new StringBuffer();
			if (!Util.execCmd(source, this.execOutput)) {
				throw new ConfigurationException("cannot parse 'exec#"
									+ source + "': " + execOutput.toString());
			}
			this.ptr = this.execOutput.toString();
			this.ptrIndex = 0;
			this.ptrLen = this.ptr.length();
			break;
		default:
			Util.assertion(false); // Bug!
			break;
		}
		nextChar(); // initialize ch
	}


	protected LexBase(String str) throws ConfigurationException
	{
		uidIdentifierProcessor = new UidIdentifierDummyProcessor();
		sourceType = Configuration.INPUT_STRING;
		source = str;
		lineNum = 1;
		ptr = str;
		ptrIndex = 0;
		ptrLen = source.length();
		this.execOutput = null;
		nextChar(); // initialize ch

		this.keywordInfoArray = null;
		this.funcInfoArray = null;
	}


	private void nextChar()
	{
		if (sourceType == Configuration.INPUT_FILE) {
			do {
				try {
					ch = file.read();
				} catch(IOException ex) {
					ch = EOF;
				}
			} while (ch == '\r');
		} else {
			do {
				if (ptrIndex == ptrLen) {
					ch = EOF;
				} else {
					ch = ptr.charAt(ptrIndex);
					ptrIndex ++;
				}
			} while (ch == '\r');
		}

		if (ch == '\n') {
			lineNum ++;
		}
	}


	private void consumeBlockString(LexToken token)
	{
		int					lineNum;
		StringBuffer		spelling;
		int					prevCh;

		//--------
		// Note the line number at the start of the block string
		//--------
		lineNum = this.lineNum;

		//--------
		// Consume chars until we get to "%>"
		//--------
		spelling = new StringBuffer();
		prevCh = ' ';
		while (!(prevCh == '%' && ch == '>')) {
			if (ch == EOF) {
				token.reset(LEX_BLOCK_STRING_WITH_EOF_SYM, lineNum,
							spelling.toString());
				return;
			}
			spelling.append((char)ch);
			prevCh = ch;
			nextChar();
		}

		//--------
		// Spelling contains the string followed by '%'.
		// Remove that unwanted terminating character.
		//--------
		spelling.deleteCharAt(spelling.length()-1);

		nextChar(); // consume the '>'

		//--------
		// At the end of the string.
		//--------
		token.reset(LEX_STRING_SYM, lineNum, spelling.toString());
		return;
	}


	private void consumeString(LexToken token)
	{
		int					lineNum;
		StringBuffer		spelling;

		Util.assertion(ch == '"');

		//--------
		// Note the line number at the start of the string
		//--------
		lineNum = this.lineNum;

		//--------
		// Consume chars until we get to the end of the sting
		//--------
		spelling = new StringBuffer();
		nextChar();
		while (ch != '"') {
			if (ch == EOF || ch == '\n') {
				token.reset(LEX_STRING_WITH_EOL_SYM, lineNum,
							spelling.toString());
				return;
			}
			switch ((char)ch) {
			case '%':
				//--------
				// Escape char in string
				//--------
				nextChar();
				if (ch == EOF || ch == '\n') {
					token.reset(LEX_STRING_WITH_EOL_SYM, lineNum,
								spelling.toString());
					return;
				}
				switch ((char)ch) {
				case 't':
					spelling.append('\t');
					break;
				case 'n':
					spelling.append('\n');
					break;
				case '%':
					spelling.append('%');
					break;
				case '"':
					spelling.append('"');
					break;
				default:
					throw new ConfigurationException(
								"Invalid escape sequence (%" + (char)ch
								+ ") in string on line " + this.lineNum);
				}
				break;
			default:
				//--------
				// Typical char in string
				//--------
				spelling.append((char)ch);
				break;
			}
			nextChar();
		}
		nextChar();	// consume the terminating double-quote char

		//--------
		// At the end of the string.
		//--------
		token.reset(LEX_STRING_SYM, lineNum, spelling.toString());
		return;
	}


	private boolean isKeywordChar(char ch)
	{
		if ('A' <= ch  && ch <= 'Z') { return true; }
		if ('a' <= ch  && ch <= 'z') { return true; }
		return false;
	}


	private boolean isIdentifierChar(char ch)
	{
		boolean				result;
		result = Character.isLetter(ch)
			|| Character.isDigit(ch)
			|| ch == '-'   // dash
			|| ch == '_'   // underscore
			|| ch == '.'   // dot
			|| ch == ':'   // For C++ nested names, e.g., x::y
			|| ch == '$'   // For names of Java nested classes
			|| ch == '?'   // For Ruby identifiers, e.g., found?
			|| ch == '/'   // For URLs, e.g., http://foo.com/bar/
			|| ch == '\\'  // For Windows directory names
			;
		return result;
	}


	private int searchForFunction(String spelling)
	{
		int			i;

		if (funcInfoArray == null) {
			return -1;
		}
		for (i = 0; i < funcInfoArray.length; i++) {
			if (spelling.equals(funcInfoArray[i].spelling)) {
				return i;
			}
		}
		return -1;
	}


	private int searchForKeyword(String spelling)
	{
		int			i;

		if (keywordInfoArray == null) {
			return -1;
		}
		for (i = 0; i < keywordInfoArray.length; i++) {
			if (spelling.equals(keywordInfoArray[i].spelling)) {
				return i;
			}
		}
		return -1;
	}


	//--------
	// Instance variables
	//--------
	private UidIdentifierProcessor	uidIdentifierProcessor;
	private int						lineNum; // Used for error reporting
	private int						ch; // Lookahead character
	private int						sourceType;
	private String					source;

	//--------
	// CFG_INPUT_FILE   uses file
	// CFG_INPUT_STRING uses ptrIndex and ptr (assigned from source)
	// CFG_INPUT_EXEC   uses ptrIndex and ptr (assigned from execOutput)
	//--------
	private BufferedReader		file;
	private String				ptr;
	private int					ptrIndex;
	private int					ptrLen;
	private StringBuffer		execOutput;

	//--------
	// The constructors of a subclass should initialize the
	// following variables.
	//--------
	protected LexKeywordInfo[]	keywordInfoArray;
	protected LexFuncInfo[]		funcInfoArray;

}
