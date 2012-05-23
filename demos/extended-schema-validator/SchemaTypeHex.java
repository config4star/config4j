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

import org.config4j.Configuration;
import org.config4j.ConfigurationException;
import org.config4j.SchemaType;
import org.config4j.SchemaValidator;


public class SchemaTypeHex extends SchemaType
{
	public SchemaTypeHex()
	{
		super("hex", Configuration.CFG_STRING);
	}


	public void checkRule(
		SchemaValidator		sv,
		Configuration		cfg,
		String				typeName,
		String[]			typeArgs,
		String				rule) throws ConfigurationException
	{
		int					len;
		int					maxDigits;

		len = typeArgs.length;
		if (len == 0) {
			return;
		} else if (len > 1) {
			throw new ConfigurationException("schema error: the '" + typeName
						+ "' type should take either no arguments or 1 "
						+ "argument (denoting max-digits) "
						+ "in rule '" + rule + "'");
		}
		try {
			maxDigits = cfg.stringToInt("", "", typeArgs[0]);
		} catch(ConfigurationException ex) {
			throw new ConfigurationException("schema error: non-integer value "
					+ "for the 'max-digits' argument in rule '" + rule + "'");
		}
		if (maxDigits < 1) {
			throw new ConfigurationException("schema error: the max-digits "
					+ "argument must be 1 or greater in rule '" + rule + "'");
		}
	}


	public boolean isA(
		SchemaValidator		sv,
		Configuration		cfg,
		String				value,
		String				typeName,
		String[] 			typeArgs,
		int					indentLevel,
		StringBuffer		errSuffix) throws ConfigurationException
	{
		int					maxDigits;

		if (!isHex(value)) {
			errSuffix.append("the value is not a hexadecimal number");
			return false;
		}
		if (typeArgs.length == 1) {
			//--------
			// Check if there are too many hex digits in the value
			//--------
			maxDigits = cfg.stringToInt("", "", typeArgs[0]);
			if (value.length() > maxDigits) {
				errSuffix.append("the value must not contain more than "
								 + maxDigits + " digits");
				return false;
			}
		}
		return true;
	}


	public static int lookupHex(
		Configuration		cfg,
		String				scope,
		String				localName) throws ConfigurationException
	{
		String				str;

		str = cfg.lookupString(scope, localName);
		return stringToHex(cfg, scope, localName, str);
	}


	public static int lookupHex(
		Configuration		cfg,
		String				scope,
		String				localName,
		int					defaultVal) throws ConfigurationException
	{
		String				str;

		if (cfg.type(scope, localName) == Configuration.CFG_NO_VALUE) {
			return defaultVal;
		}
		str = cfg.lookupString(scope, localName);
		return stringToHex(cfg, scope, localName, str);
	}


	public static int stringToHex(
		Configuration		cfg,
		String				scope,
		String				localName,
		String				str) throws ConfigurationException
	{
		return stringToHex(cfg, scope, localName, str, "hex");
	}


	public static int stringToHex(
		Configuration		cfg,
		String				scope,
		String				localName,
		String				str,
		String				typeName) throws ConfigurationException
	{
		String				fullyScopedName;

		try {
			return (int)Long.parseLong(str, 16);
		} catch(NumberFormatException ex) {
			fullyScopedName = cfg.mergeNames(scope, localName);
			throw new ConfigurationException(cfg.fileName() + ": bad "
						+ typeName + " value ('" + str + "') specified for '"
						+ fullyScopedName);
		}
	}


	public static boolean isHex(String str)
	{
		try {
			Long.parseLong(str, 16);
			return true;
		} catch(NumberFormatException ex) {
			return false;
		}
	}

}

