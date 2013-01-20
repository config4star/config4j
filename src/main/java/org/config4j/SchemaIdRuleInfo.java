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

class SchemaIdRuleInfo implements Comparable<SchemaIdRuleInfo> {

	private String[] args;

	private boolean isOptional;

	// --------
	// Instance variables
	// --------
	private String locallyScopedName;

	private String typeName;

	public SchemaIdRuleInfo(String locallyScopedName, String typeName, ArrayList<String> args, boolean isOptional) {
		this.locallyScopedName = locallyScopedName;
		this.typeName = typeName;
		this.args = args.toArray(new String[args.size()]);
		this.isOptional = isOptional;
	}

	public SchemaIdRuleInfo(String locallyScopedName, String typeName, String[] args, boolean isOptional) {
		this.locallyScopedName = locallyScopedName;
		this.typeName = typeName;
		this.args = args;
		this.isOptional = isOptional;
	}

	public int compareTo(SchemaIdRuleInfo o) {
		return locallyScopedName.compareTo(o.locallyScopedName);
	}

	@Override
	public boolean equals(Object o) {
		SchemaIdRuleInfo other = (SchemaIdRuleInfo) o;
		return locallyScopedName.equals(other.locallyScopedName);
	}

	public String[] getArgs() {
		return args;
	}

	public boolean getIsOptional() {
		return isOptional;
	}

	public String getLocallyScopedName() {
		return locallyScopedName;
	}

	public String getTypeName() {
		return typeName;
	}
}
