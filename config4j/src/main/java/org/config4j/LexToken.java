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

class LexToken {
	private short funcType;

	private int lineNum;

	private String spelling;

	// --------
	// Instance variables.
	// --------
	private short type;

	// --------
	// Ctor, dtor and assignment operator
	// --------
	public LexToken() {
		type = LexBase.LEX_UNKNOWN_SYM;
		lineNum = -1;
		spelling = "";
		funcType = LexBase.NOT_A_FUNC;
	}

	public LexToken(LexToken other) {
		type = other.type;
		lineNum = other.lineNum;
		spelling = other.spelling;
		funcType = other.funcType;
	}

	public LexToken(short type, int lineNum, String spelling) {
		this.type = type;
		this.lineNum = lineNum;
		this.spelling = spelling;
		funcType = LexBase.NOT_A_FUNC;
	}

	public int getLineNum() {
		return lineNum;
	}

	// --------
	// Accessor functions
	// --------
	public String getSpelling() {
		return spelling;
	}

	public int getType() {
		return type;
	}

	public boolean isBoolFunc() {
		return funcType == LexBase.BOOL_FUNC;
	}

	public boolean isListFunc() {
		return funcType == LexBase.LIST_FUNC;
	}

	public boolean isStringFunc() {
		return funcType == LexBase.STRING_FUNC;
	}

	// --------
	// Modifier function
	// --------
	public void reset(short type, int lineNum, String spelling) {
		this.type = type;
		this.lineNum = lineNum;
		this.spelling = spelling;
		funcType = LexBase.NOT_A_FUNC;
	}

	public void reset(short type, int lineNum, String spelling, short funcType) {
		this.type = type;
		this.lineNum = lineNum;
		this.spelling = spelling;
		this.funcType = funcType;
	}
}
