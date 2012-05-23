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


//--------
// Class Configuration
//--------

public abstract class Configuration
{
	// Type
	public static final int CFG_NO_VALUE       = 0; // bit masks
	public static final int CFG_STRING         = 1; // 0001
	public static final int CFG_LIST           = 2; // 0010
	public static final int CFG_SCOPE          = 4; // 0100
	public static final int CFG_VARIABLES      = 3; // 0011 = STRING|LIST
	public static final int CFG_SCOPE_AND_VARS = 7; // 0111 = STRING|LIST|SCOPE

    // Constant values used for sourceType in parse()
	public static final int INPUT_FILE   = 1;
	public static final int INPUT_STRING = 2;
	public static final int INPUT_EXEC   = 3;


	public static Configuration create()
	{
		return new ConfigurationImpl();
	}


	public static boolean patternMatch(String str, String pattern)
	{
		return patternMatchInternal(str, 0, str.length(), pattern, 0,
									pattern.length());
	}


	public static String mergeNames(String scope, String localName)
	{
		if (scope.length() == 0) {
			return localName;
		} else if (localName.length() == 0) {
			return scope;
		} else {
			return scope + "." + localName;
		}
	}


	public abstract void setFallbackConfiguration(Configuration cfg);

	public abstract void setFallbackConfiguration(
		int			sourceType,
		String		source) throws ConfigurationException;

	public abstract void setFallbackConfiguration(
		int			sourceType,
		String		source,
		String		sourceDescription) throws ConfigurationException;

	public abstract Configuration getFallbackConfiguration();

	public abstract void setSecurityConfiguration(Configuration cfg)
												throws ConfigurationException; 

	public abstract void setSecurityConfiguration(
		Configuration	cfg,
		String			scope) throws ConfigurationException; 

	public abstract void setSecurityConfiguration(String cfgInput)
												throws ConfigurationException; 

	public abstract void setSecurityConfiguration(
		String		cfgInput,
		String		scope) throws ConfigurationException; 

	public abstract Configuration getSecurityConfiguration();

	public abstract String getSecurityConfigurationScope();

	public abstract String getenv(String name);

	public abstract void parse(String sourceTypeAndSource)
												throws ConfigurationException;

	public abstract void parse(
		int			sourceType,
		String		source) throws ConfigurationException;

	public abstract void parse(
		int			sourceType,
		String		source,
		String		sourceDescription) throws ConfigurationException;

	public abstract String fileName();

	public abstract String[] listFullyScopedNames(
		String		scope,
		String		localName,
		int			typeMask,
		boolean		recursive) throws ConfigurationException;

	public abstract String[] listFullyScopedNames(
		String		scope,
		String		localName,
		int			typeMask,
		boolean		recursive,
		String		filterPattern) throws ConfigurationException;

	public abstract String[] listFullyScopedNames(
		String		scope,
		String		localName,
		int			typeMask,
		boolean		recursive,
		String []	filterPatterns) throws ConfigurationException;

	public abstract String[] listLocallyScopedNames(
		String		scope,
		String		localName,
		int			typeMask,
		boolean		recursive) throws ConfigurationException;

	public abstract String[] listLocallyScopedNames(
		String		scope,
		String		localName,
		int			typeMask,
		boolean		recursive,
		String		filterPattern) throws ConfigurationException;

	public abstract String[] listLocallyScopedNames(
		String		scope,
		String		localName,
		int			typeMask,
		boolean		recursive,
		String []	filterPatterns) throws ConfigurationException;

	public abstract int type(String scope, String localName);

	public abstract boolean uidEquals(String str1, String str2);

	public abstract String expandUid(String str) throws ConfigurationException;

	public abstract String unexpandUid(String spelling)
												throws ConfigurationException;

	//--------
	// Dump part or all of the configuration
	//--------
	public abstract String dump(boolean wantExpandedUidNames);

	public abstract String dump(
		 boolean	wantExpandedUidNames,
		 String		scope,
		 String		localName) throws ConfigurationException;

	public abstract boolean isBoolean(String str);
	public abstract boolean isInt(String str);
	public abstract boolean isFloat(String str);
	public abstract boolean isDurationMicroseconds(String str);
	public abstract boolean isDurationMilliseconds(String str);
	public abstract boolean isDurationSeconds(String str);
	public abstract boolean isMemorySizeBytes(String str);
	public abstract boolean isMemorySizeKB(String str);
	public abstract boolean isMemorySizeMB(String str);

	public abstract boolean isEnum(
		String				str,
		EnumNameAndValue[]	enumInfo);

	public abstract boolean isFloatWithUnits(
		String				str,
		String[]			allowedUnits);

	public abstract boolean isIntWithUnits(
		String				str,
		String[]			allowedUnits);

	public abstract boolean isUnitsWithFloat(
		String				str,
		String[]			allowedUnits);

	public abstract boolean isUnitsWithInt(
		String			str,
		String[]		allowedUnits);

	public abstract boolean stringToBoolean(
		String			scope,
		String			localName,
		String			str) throws ConfigurationException;

	public abstract int stringToInt(
		String			scope,
		String			localName,
		String			str) throws ConfigurationException;

	public abstract float stringToFloat(
		String			scope,
		String			localName,
		String			str) throws ConfigurationException;

	public abstract int stringToDurationSeconds(
		String			scope,
		String			localName,
		String			str) throws ConfigurationException;

	public abstract int stringToDurationMicroseconds(
		String			scope,
		String			localName,
		String			str) throws ConfigurationException;

	public abstract int stringToDurationMilliseconds(
		String			scope,
		String			localName,
		String			str) throws ConfigurationException;

	public abstract int stringToMemorySizeBytes(
		String				scope,
		String				localName,
		String				str) throws ConfigurationException;

	public abstract int stringToMemorySizeKB(
		String				scope,
		String				localName,
		String				str) throws ConfigurationException;

	public abstract int stringToMemorySizeMB(
		String				scope,
		String				localName,
		String				str) throws ConfigurationException;

	public abstract int stringToEnum(
		String				scope,
		String				localName,
		String				type,
		String				str,
		EnumNameAndValue[]	enumInfo) throws ConfigurationException;

	public abstract ValueWithUnits stringToFloatWithUnits(
		String				scope,
		String				localName,
		String				typeName,
		String				str,
		String[]			allowedUnits) throws ConfigurationException;

	public abstract ValueWithUnits stringToUnitsWithFloat(
		String				scope,
		String				localName,
		String				typeName,
		String				str,
		String[]			allowedUnits) throws ConfigurationException;

	public abstract ValueWithUnits stringToIntWithUnits(
		String				scope,
		String				localName,
		String				typeName,
		String				str,
		String[]			allowedUnits) throws ConfigurationException;

	public abstract ValueWithUnits stringToUnitsWithInt(
		String				scope,
		String				localName,
		String				typeName,
		String				str,
		String[]			allowedUnits) throws ConfigurationException;

	//--------
	// lookup<Type>() operations, with and without default values.
	//--------
	public abstract String lookupString(
		String		scope,
		String		localName,
		String		defaultVal) throws ConfigurationException;

	public abstract String lookupString(String scope, String localName)
											throws ConfigurationException;

	public abstract String[] lookupList(
		String		scope,
		String		localName,
		String[]	defaultArray) throws ConfigurationException;

	public abstract String[] lookupList(String scope, String localName)
												throws ConfigurationException;

	public abstract int lookupInt(
		String		scope,
		String		localName,
		int			defaultVal) throws ConfigurationException;

	public abstract int lookupInt(String scope, String localName)
											throws ConfigurationException;

	public abstract float lookupFloat(
		String		scope,
		String		localName,
		float		defaultVal) throws ConfigurationException;

	public abstract float lookupFloat(String scope, String localName)
												throws ConfigurationException;

	public abstract int lookupEnum(
		String				scope,
		String				localName,
		String				typeName,
		EnumNameAndValue[]	enumInfo,
		String				defaultVal) throws ConfigurationException;

	public abstract int lookupEnum(
		String				scope,
		String				localName,
		String				typeName,
		EnumNameAndValue[]	enumInfo,
		int					defaultVal) throws ConfigurationException;

	public abstract int lookupEnum(
		String				scope,
		String				localName,
		String				typeName,
		EnumNameAndValue[]	enumInfo) throws ConfigurationException;

	public abstract boolean lookupBoolean(
		String				scope,
		String				localName,
		boolean				defaultVal) throws ConfigurationException;

	public abstract boolean lookupBoolean(String scope, String localName)
												throws ConfigurationException;

	public abstract ValueWithUnits lookupFloatWithUnits(
		String			scope,
		String			localName,
		String			typeName,
		String[]		allowedUnits) throws ConfigurationException;

	public abstract ValueWithUnits lookupFloatWithUnits(
		String				scope,
		String				localName,
		String				typeName,
		String[]			allowedUnits,
		ValueWithUnits		defaultValueWithUnits)
												throws ConfigurationException;

	public abstract ValueWithUnits lookupUnitsWithFloat(
		String			scope,
		String			localName,
		String			typeName,
		String[]		allowedUnits) throws ConfigurationException;

	public abstract ValueWithUnits lookupUnitsWithFloat(
		String				scope,
		String				localName,
		String				typeName,
		String[]			allowedUnits,
		ValueWithUnits		defaultValueWithUnits)
												throws ConfigurationException;

	public abstract ValueWithUnits lookupIntWithUnits(
		String			scope,
		String			localName,
		String			typeName,
		String[]		allowedUnits) throws ConfigurationException;

	public abstract ValueWithUnits lookupIntWithUnits(
		String				scope,
		String				localName,
		String				typeName,
		String[]			allowedUnits,
		ValueWithUnits		defaultValueWithUnits)
												throws ConfigurationException;

	public abstract ValueWithUnits lookupUnitsWithInt(
		String			scope,
		String			localName,
		String			typeName,
		String[]		allowedUnits) throws ConfigurationException;

	public abstract ValueWithUnits lookupUnitsWithInt(
		String				scope,
		String				localName,
		String				typeName,
		String[]			allowedUnits,
		ValueWithUnits		defaultValueWithUnits)
												throws ConfigurationException;

	public abstract int lookupDurationMicroseconds(
		String			scope,
		String			localName,
		int				defaultVal) throws ConfigurationException;

	public abstract int lookupDurationMicroseconds(
		String			scope,
		String			localName) throws ConfigurationException;

	public abstract int lookupDurationMilliseconds(
		String			scope,
		String			localName,
		int				defaultVal) throws ConfigurationException;

	public abstract int lookupDurationMilliseconds(
		String			scope,
		String			localName) throws ConfigurationException;

	public abstract int lookupDurationSeconds(
		String			scope,
		String			localName,
		int				defaultVal) throws ConfigurationException;

	public abstract int lookupDurationSeconds(
		String			scope,
		String			localName) throws ConfigurationException;

	public abstract int lookupMemorySizeBytes(
		String			scope,
		String			localName,
		int				defaultVal) throws ConfigurationException;

	public abstract int lookupMemorySizeBytes(
		String			scope,
		String			localName) throws ConfigurationException;

	public abstract int lookupMemorySizeKB(
		String			scope,
		String			localName,
		int				defaultVal) throws ConfigurationException;

	public abstract int lookupMemorySizeKB(
		String			scope,
		String			localName) throws ConfigurationException;

	public abstract int lookupMemorySizeMB(
		String			scope,
		String			localName,
		int				defaultVal) throws ConfigurationException;

	public abstract int lookupMemorySizeMB(
		String			scope,
		String			localName) throws ConfigurationException;

	public abstract void lookupScope(
		String			scope,
		String			localName) throws ConfigurationException;

	//--------
	// Update operations
	//--------

	public abstract void	insertString(
		String			scope,
		String			localName,
		String			strValue) throws ConfigurationException;

	public abstract void	insertList(
		String			scope,
		String			localName,
		String[]		listValue) throws ConfigurationException;

	public abstract void	ensureScopeExists(
		String			scope,
		String			localName) throws ConfigurationException;

	public abstract void remove(String scope, String localName)
												throws ConfigurationException;

	public abstract void empty();

	protected Integer enumVal(
		String				name,
		EnumNameAndValue[]	enumInfo)
	{
		for (int i = 0; i < enumInfo.length; i++) {
			if (name.equals(enumInfo[i].getName())) {
				return new Integer(enumInfo[i].getValue());
			}
		}
		return null;
	}

	private static boolean patternMatchInternal(
		String		str,
		int			strIndex,
		int			strLen,
		String		 pattern,
		int			patternIndex,
		int			patternLen)
	{
		while (patternIndex < patternLen) {
			if (pattern.charAt(patternIndex) != '*') {
				if (strIndex == strLen
				    || pattern.charAt(patternIndex) != str.charAt(strIndex))
				{
					return false;
				}
				patternIndex++;
				if (strIndex < strLen) {
					strIndex++;
				}
			} else {
				patternIndex++;
				while (patternIndex < patternLen
					&& pattern.charAt(patternIndex) == '*')
				{
					patternIndex++;
				}
				if (patternIndex == patternLen) {
					return true;
				}
				for (; strIndex < strLen; strIndex++) {
					if (patternMatchInternal(str, strIndex, strLen, pattern,
											 patternIndex, patternLen))
					{
						return true;
					}
				}
			}
		}
		if (strIndex != strLen) {
			return false;
		}
		return true;
	}
}
