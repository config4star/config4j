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


class LexToken
{
	//--------
	// Ctor, dtor and assignment operator
	//--------
	public LexToken()
	{
		this.type     = LexBase.LEX_UNKNOWN_SYM;
		this.lineNum  = -1;
		this.spelling = "";
		this.funcType = LexBase.NOT_A_FUNC;
	}


	public LexToken(LexToken other)
	{
		this.type     = other.type;
		this.lineNum  = other.lineNum;
		this.spelling = other.spelling;
		this.funcType = other.funcType;
	}


	public LexToken(short type, int lineNum, String spelling)
	{
		this.type     = type;
		this.lineNum  = lineNum;
		this.spelling = spelling;
		this.funcType = LexBase.NOT_A_FUNC;
	}

	//--------
	// Accessor functions
	//--------
	public String getSpelling()   { return this.spelling; }
	public int getLineNum()       { return this.lineNum; }
	public int getType()          { return this.type; }
	public boolean isStringFunc() { return this.funcType==LexBase.STRING_FUNC; }
	public boolean isListFunc()	  { return this.funcType == LexBase.LIST_FUNC; }
	public boolean isBoolFunc()	  { return this.funcType == LexBase.BOOL_FUNC; }

	//--------
	// Modifier function
	//--------
	public void reset(short type, int lineNum, String spelling)
	{
		this.type     = type;
		this.lineNum  = lineNum;
		this.spelling = spelling;
		this.funcType = LexBase.NOT_A_FUNC;
	}


	public void reset(
		short			type,
		int				lineNum,
		String			spelling,
		short			funcType)
	{
		this.type     = type;
		this.lineNum  = lineNum;
		this.spelling = spelling;
		this.funcType = funcType;
	}

	//--------
	// Instance variables.
	//--------
	private short		type;
	private String		spelling;
	private int			lineNum;
	private short		funcType;
}
