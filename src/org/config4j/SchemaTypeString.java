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


class SchemaTypeString extends SchemaType
{

	public SchemaTypeString()
	{
		super("string", Configuration.CFG_STRING);
	}


	public void checkRule(
		SchemaValidator		sv,
		Configuration		cfg,
		String				typeName,
		String[]			typeArgs,
		String				rule) throws ConfigurationException
	{
		int					len;
		int					min;
		int					max;

		len = typeArgs.length;
		if (len == 0) {
			return;
		}
		if (len != 2) {
			throw new ConfigurationException("the '" + typeName
					+ "' type should take either no arguments or 2 arguments "
					+ "(denoting min_length and max_length values) in rule '"
					+ rule + "'");
		}
		try {
			min = cfg.stringToInt("", "", typeArgs[0]);
		} catch(ConfigurationException ex) {
			throw new ConfigurationException("non-integer value for the first "
						+ "('min_length') argument in rule '" + rule + "'");
		}
		try {
			max = cfg.stringToInt("", "", typeArgs[1]);
		} catch(ConfigurationException ex) {
			throw new ConfigurationException("non-integer value for the second "
					+ "('max_length') argument in rule '" + rule + "'");
		}
		if (min < 0 || max < 0) {
			throw new ConfigurationException("the 'min_length' and "
					+ "'max_length' of a string cannot be negative in rule '"
					+ rule + "'");
		}
		if (min > max) {
			throw new ConfigurationException("the first ('min_length') "
					+ "argument is larger than the second ('max_length') "
					+ "argument in rule '" + rule + "'");
		}
	}


	public boolean isA(
		SchemaValidator		sv,
		Configuration		cfg,
		String				value,
		String				typeName,
		String[]			typeArgs,
		int					indentLevel,
		StringBuffer		errSuffix) throws ConfigurationException
	{
		int					strLen;
		int					minLength;
		int					maxLength;

		if (typeArgs.length == 0) {
			return true;
		}
		strLen = value.length();
		minLength = cfg.stringToInt("", "", typeArgs[0]);
		maxLength = cfg.stringToInt("", "", typeArgs[1]);
		if (strLen < minLength || strLen > maxLength) {
			errSuffix.append("its length is outside the permitted range ["
							 +  minLength + ", " + maxLength + "]");
			return false;
		}
		return true;
	}

}
