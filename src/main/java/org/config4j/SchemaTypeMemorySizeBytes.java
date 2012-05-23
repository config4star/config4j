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


class SchemaTypeMemorySizeBytes extends SchemaType
{

	public SchemaTypeMemorySizeBytes()
	{
		super("memorySizeBytes", Configuration.CFG_STRING);
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
					+ "(denoting min and max memory sizes) in rule '"
					+ rule + "'");
		}
		try {
			min = cfg.stringToMemorySizeBytes("", "", typeArgs[0]);
		} catch(ConfigurationException ex) {
			throw new ConfigurationException("bad " + typeName
					+ " value for the first ('min') argument in rule '"
					+ rule + "'; should be the format '<float> <units>' "
					+ "where <units> is one of: "
					+ "'byte', 'bytes', 'KB', 'MB', 'GB'");
		}
		try {
			max = cfg.stringToMemorySizeBytes("", "", typeArgs[1]);
		} catch(ConfigurationException ex) {
			throw new ConfigurationException("bad " + typeName
					+ " value for the second ('max') argument in rule '"
					+ rule + "'; should be in the format '<float> <units>' "
					+ "where <units> is one of: "
					+ "'byte', 'bytes', 'KB', 'MB', 'GB'");
		}
		if (min < -1 || max < -1) {
			throw new ConfigurationException("the 'min' and 'max' of a "
					+ typeName + " cannot be negative in rule '" + rule + "'");
		}
		if ((max != -1) && (min == -1 || min > max)) {
			throw new ConfigurationException("the first ('min') argument "
					+ "is larger than the second ('max') argument in rule '"
					+ rule + "'");
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
		int					val;
		int					min;
		int					max;

		try {
			val = cfg.stringToMemorySizeBytes("", "", value);
		} catch(ConfigurationException ex) {
			errSuffix.append("the value should be in the format "
					+ "'<units> <float>' where <units> is one of: "
					+ "'byte', 'bytes', 'KB', 'MB', 'GB'");
			return false;
		}
		if (typeArgs.length == 0) {
			return true;
		}
		min = cfg.stringToMemorySizeBytes("", "", typeArgs[0]);
		max = cfg.stringToMemorySizeBytes("", "", typeArgs[1]);
		if (val < min || val > max) {
			errSuffix.append("the value is outside the permitted range ["
					  + typeArgs[0] + ", " + typeArgs[1] + "]");
			return false;
		}
		return true;
	}

}
