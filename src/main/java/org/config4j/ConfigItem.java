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


//--------------------------------------------------------------
// Class:	ConfigItem
//
// Description:	A config file contains "name = <value>" statements
//		and "name <scope>" statements.
//		This class is used to store name plus the the
//		<value> part, (which can be a string or a sequence
//		of string) or a <scope>.
//--------------------------------------------------------------
class ConfigItem
{
	ConfigItem(String name, String stringVal)
	{
		this.name      = name;
		this.stringVal = stringVal;
		this.type      = Configuration.CFG_STRING;
	}


	ConfigItem(String name, String[] listVal)
	{
		this.name    = name;
		this.listVal = listVal;
		this.type    = Configuration.CFG_LIST;
	}


	ConfigItem(String name, ConfigScope scopeVal)
	{
		this.name     = name;
		this.scopeVal = scopeVal;
		this.type     = Configuration.CFG_SCOPE;
	}


	//--------
	// Public operations
	//--------

	int getType()
	{
		return this.type;
	}


	String getName()
	{
		return this.name;
	}


	String getStringVal()
	{
		Util.assertion(type == Configuration.CFG_STRING);
		return this.stringVal;
	}


	String[] getListVal()
	{
		Util.assertion(type == Configuration.CFG_LIST);
		return this.listVal;
	}


	ConfigScope getScopeVal()
	{
		Util.assertion(type == Configuration.CFG_SCOPE);
		return this.scopeVal;
	}


	void dump(StringBuffer buf, String name, boolean wantExpandedUidNames)
	{
		dump(buf, name, wantExpandedUidNames, 0);
	}


	void dump(
		StringBuffer				buf,
		String						name,
		boolean						wantExpandedUidNames,
		int							indentLevel)
	{
		int							i;
		String						escStr;
		UidIdentifierProcessor		uidIdProc;

		if (!wantExpandedUidNames) {
			uidIdProc = new UidIdentifierProcessor();
			name = uidIdProc.unexpand(name);
		}
		printIndent(buf, indentLevel);
		switch(type) {
		case Configuration.CFG_STRING:
			escStr = escapeString(stringVal);
			buf.append(name + " = \"" + escStr + "\";\n");
			break;
		case Configuration.CFG_LIST:
			buf.append(name + " = [");
			for (i = 0; i < listVal.length; i++) {
				escStr = escapeString(listVal[i]);
				buf.append("\"" + escStr + "\"");
				if (i < listVal.length-1) {
					buf.append(", ");
				}
			}
			buf.append("];\n");
			break;
		case Configuration.CFG_SCOPE:
			buf.append(name + " {\n");
			scopeVal.dump(buf, wantExpandedUidNames, indentLevel+1);
			printIndent(buf, indentLevel);
			buf.append("}\n");
			break;
		default:
			Util.assertion(false); // Bug!
		}
	}


	private void printIndent(StringBuffer buf, int indentLevel)
	{
		int				i;

		for (i = 0; i < indentLevel; i++) {
			buf.append("\t");
		}
	}


	private String escapeString(String str)
	{
		StringBuffer		buf;
		int					i;
		int					len;
		char				ch;

		buf = new StringBuffer();
		len = str.length();
		for (i = 0; i < len; i++) {
			ch = str.charAt(i);
			switch(ch) {
			case '\t':
				buf.append("%t");
				break;
			case '\n':
				buf.append("%n");
				break;
			case '%':
				buf.append("%%");
				break;
			case '"':
				buf.append("%\"");
				break;
			default:
				buf.append(ch);
				break;
			}
		}
		return buf.toString();
	}


	private int				type;
	private String			name;
	private String			stringVal;
	private String[]		listVal;
	private ConfigScope		scopeVal;
}

