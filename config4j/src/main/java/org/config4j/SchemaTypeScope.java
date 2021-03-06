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

class SchemaTypeScope extends SchemaType {

	public SchemaTypeScope() {
		super("scope", Configuration.CFG_SCOPE);
	}

	@Override
	public void checkRule(SchemaValidator sv, Configuration cfg, String typeName, String[] typeArgs, String rule)
	        throws ConfigurationException {
		if (typeArgs.length != 0) {
			throw new ConfigurationException("the '" + typeName + "' type " + "should not take arguments in rule '" + rule + "'");
		}
	}

	@Override
	public void validate(SchemaValidator sv, Configuration cfg, String scope, String name, String typeName, String origTypeName,
	        String[] typeArgs, int indentLevel) throws ConfigurationException {
		cfg.lookupScope(scope, name);
	}

}
