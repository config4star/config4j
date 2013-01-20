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

class UidIdentifierProcessor {
	private int count;

	public UidIdentifierProcessor() {
		count = 0;
	}

	private String formatDigits(int num) {
		char[] digits;
		int i;
		int remainder;

		digits = new char[9];
		for (i = 0; i < digits.length; i++) {
			remainder = num % 10;
			num = num / 10;
			digits[9 - (i + 1)] = (char) ('0' + remainder);
		}
		return new String(digits);
	}

	/**
	 * str must be in one of the following forms: - "foo" --> "foo" - "uid-<foo>" --> "uid-<digits>-<foo>" - "uid-<digits>-<foo>" -->
	 * "uid-<new-digits>-<foo>" where "<foo>" does NOT start with a digit or "-"
	 **/
	public String expand(String str) throws ConfigurationException {
		String array[] = null;
		StringBuffer result = null;
		int i;

		// --------
		// Common case optimizations
		// --------
		if (str.indexOf('.') == -1) {
			return expandOne(str);
		}
		if (str.indexOf("uid-") == -1) {
			return str;
		}

		// --------
		// Let's break apart the scoped name, expand each local part
		// and then recombine the parts into an expanded scoped name.
		// --------
		result = new StringBuffer();
		array = Util.splitScopedNameIntoArray(str);
		for (i = 0; i < array.length; i++) {
			try {
				result.append(expandOne(array[i]));
			} catch (ConfigurationException ex) {
				throw new ConfigurationException("expand(): " + "'" + str + "' is not a legal identifier");
			}
			if (i < array.length - 1) {
				result.append(".");
			}
		}
		return result.toString();
	}

	public StringBuffer expand(StringBuffer buf) throws ConfigurationException {
		return new StringBuffer(expand(buf.toString()));

	}

	private String expandOne(String str) throws ConfigurationException {
		String msg;
		String suffix;
		String digits;
		String result;
		char ch;
		int i;
		int len;
		int numDigits;

		msg = "'" + str + "' is not a legal identifier";

		// --------
		// If str does not start with "uid-" then do nothing.
		// --------
		if (!str.startsWith("uid-")) {
			return str;
		}

		// --------
		// Check for "uid-" (with no suffix), because that is illegal
		// --------
		len = str.length();
		if (len == 4) {
			throw new ConfigurationException(msg);
		}
		ch = str.charAt(4);
		if (ch == '-') {
			// illegal: "uid--foo"
			throw new ConfigurationException(msg);
		}

		if (ch < '0' || ch > '9') {
			// --------
			// "uid-foo" --> "uid-<digits>-foo"
			// --------
			Util.assertion(count < 1000 * 1000 * 1000);
			digits = formatDigits(count);
			count++;
			suffix = str.substring(4);
			result = "uid-" + digits + "-" + suffix;
			return result;
		}

		i = 4;
		numDigits = 0;
		ch = str.charAt(i);
		while (ch >= '0' && ch <= '9') {
			i++;
			numDigits++;
			if (i == len) {
				break;
			}
			ch = str.charAt(i);
		}
		Util.assertion(numDigits > 0);
		if (i == len || str.charAt(i) != '-') {
			// illegal: "uid-<digits>" or "uid-<digits>foo"
			throw new ConfigurationException(msg);
		}
		i++; // point to just after "uid-<digits>-"
		if (i == len) {
			// illegal: "uid-<digits>-"
			throw new ConfigurationException(msg);
		}
		ch = str.charAt(i);
		if (ch == '-') {
			// illegal: "uid-<digits>--"
			throw new ConfigurationException(msg);
		}
		if (ch >= '0' && ch <= '9') {
			// illegal: "uid-<digits>-<digits>foo"
			throw new ConfigurationException(msg);
		}
		Util.assertion(count < 1000 * 1000 * 1000);
		digits = formatDigits(count);
		count++;
		suffix = str.substring(i);
		result = "uid-" + digits + "-" + suffix;
		return result;
	}

	public String unexpand(String str) throws ConfigurationException {
		// --------
		// Common case optimizations
		// --------
		if (str.indexOf('.') == -1) {
			return unexpandOne(str);
		}
		if (str.indexOf("uid-") == -1) {
			return str;
		}

		// --------
		// Let's break apart the scoped name, unexpand each local part
		// and then recombine the parts into an unexpanded scoped name.
		// --------
		String array[] = null;
		StringBuffer result;
		String msg;
		int i;
		int len;

		result = new StringBuffer();
		array = Util.splitScopedNameIntoArray(str);
		len = array.length;
		for (i = 0; i < len; i++) {
			try {
				result.append(unexpandOne(array[i]));
			} catch (ConfigurationException ex) {
				msg = "'" + str + "' is not a legal identifier";
				throw new ConfigurationException(msg);
			}
			if (i < len - 1) {
				result.append(".");
			}
		}
		return result.toString();
	}

	public StringBuffer unexpand(StringBuffer buf) throws ConfigurationException {
		return new StringBuffer(unexpand(buf.toString()));
	}

	private String unexpandOne(String str) throws ConfigurationException {
		int numDigits;
		int i;
		int len;
		char ch;
		String suffix;
		String result;

		// --------
		// If str does not start with "uid-<digits>-"
		// then do nothing.
		// --------
		if (!str.startsWith("uid-")) {
			return str;
		}
		len = str.length();
		i = 4;
		numDigits = 0;
		ch = str.charAt(i);
		while (ch >= '0' && ch <= '9') {
			i++;
			numDigits++;
			if (i == len) {
				break;
			}
			ch = str.charAt(i);
		}
		if (numDigits == 0 || i == len || ch != '-') {
			return str;
		}

		// --------
		// Okay, let's returned a modified str.
		// --------
		suffix = str.substring(i + 1, str.length());
		result = "uid-" + suffix;
		return result;
	}

}
