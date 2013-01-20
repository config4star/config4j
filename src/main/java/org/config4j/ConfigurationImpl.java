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

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;


public class ConfigurationImpl extends Configuration
{
	public ConfigurationImpl()
	{
		fileName               = "<no file>";
		rootScope              = new ConfigScope(null, "");
		currScope              = rootScope;
		fallbackCfg             = null;
		fileNameStack          = new ArrayList<String>();
		securityCfgScope       = "";
		env                    = null;
		uidIdentifierProcessor = new UidIdentifierProcessor();
		securityCfg = DefaultSecurityConfiguration.singleton;
	}


	public void setFallbackConfiguration(Configuration cfg)
	{
		fallbackCfg = (ConfigurationImpl)cfg;
	}


	public void setFallbackConfiguration(
		int			sourceType,
		String		source) throws ConfigurationException
	{
		setFallbackConfiguration(sourceType, source, "");
	}


	public void setFallbackConfiguration(
		int				sourceType,
		String			source,
		String			sourceDescription) throws ConfigurationException
	{
		Configuration	cfg;

		cfg = Configuration.create();
		try {
			cfg.parse(sourceType, source, sourceDescription);
		} catch(ConfigurationException ex) {
			throw new ConfigurationException("cannot set default "
					+ "configuration: " + ex.getMessage());
		}

		fallbackCfg = (ConfigurationImpl)cfg;
	}


	public Configuration getFallbackConfiguration()
	{
		return fallbackCfg;
	}


	public void setSecurityConfiguration(Configuration cfg)
												throws ConfigurationException
	{
		setSecurityConfiguration(cfg, "");
	}


	public void setSecurityConfiguration(Configuration cfg, String scope)
												throws ConfigurationException
	{
		try {
			cfg.lookupList(scope, "allow_patterns");
			cfg.lookupList(scope, "deny_patterns");
			cfg.lookupList(scope, "trusted_directories");
		} catch(ConfigurationException ex) {
			throw new ConfigurationException("cannot set security "
				+ "configuration: " + ex.getMessage());
		}

		securityCfg = cfg;
		securityCfgScope = scope;
	}


	public void setSecurityConfiguration(String cfgInput)
												throws ConfigurationException
	{
		setSecurityConfiguration(cfgInput, "");
	}


	public void setSecurityConfiguration(
			String			cfgInput,
			String			scope) throws ConfigurationException
	{
		Configuration		cfg;

		cfg = Configuration.create();
		try {
			cfg.parse(cfgInput);
			cfg.lookupList(scope, "allow_patterns");
			cfg.lookupList(scope, "deny_patterns");
			cfg.lookupList(scope, "trusted_directories");
		} catch(ConfigurationException ex) {
			throw new ConfigurationException("cannot set security "
										+ "configuration: " + ex.getMessage());
		}
		securityCfg = cfg;
		securityCfgScope = scope;
	}


	public Configuration getSecurityConfiguration()
	{
		return securityCfg;
	}


	public String getSecurityConfigurationScope()
	{
		return securityCfgScope;
	}


	public synchronized String getenv(String name) throws ConfigurationException
	{
		String					result;
		String[]				cmd;
		String					osName;
		Process					p;
		EnvStreamParserThread	outThread;
		StreamReaderThread		errThread;

		if (env != null) {
			return env.getProperty(name);
		}

		//--------
		// Apparently, System.getenv() worked in Java 1.0. Then it was
		// "deprecated" in newer versions (because not all operating
		// systems support the concept of environment variables).
		// Actually, "deprecated" should mean that the method still works
		// but you are advised to not use it as it might be removed in a
		// future version. However, the method throws an Error (a subtype
		// of Throwable), which is not what you would expect of something
		// that is said to be "deprecated". Anyway, Java 1.5 undeprecated
		// System.getenv() and the method now returns null if the
		// environment variable does not exist or the operating system
		// does not support environment variables.
		//
		// We first try calling System.getenv(). If that fails, then we
		// execute an external command such as "env" on UNIX or
		// "cmd /c set" on Windows. Those commands print a list of
		// name=value lines, which we parse to obtain all the environment
		// variables. We do NOT feed this output into Properties.load()
		// because there might be occurrances of a backslash followed by
		// "u" in the values of environment variables, and the Properties
		// file parser would try to interpret this as the start of an
		// escape code for a Unicode character.
		//
		// Note: the idea of exectuting an OS-specific command to access
		// environment variables is inspired by similar functionality in
		// Apache Ant, but the code here is a slight bit different. If you
		// are interested in looking at the Apache Ant code, then look at
		// the following classes:
		//     org.apache.tools.ant.taskdefs.Execute
		//     org.apache.tools.ant.taskdefs.condition.Os
		//--------
		
		//--------
		// Plan A: see if System.getenv() works. We use reflection to avoid
		// compiler warnings about using a deprecated method.
		//--------
		try {
			Class c = System.class;
			Method m = c.getMethod("getenv", new Class[]{String.class});
			Object obj = m.invoke(null, new Object[]{name});
			result = (String)obj;
			return result;
		} catch(Throwable ex) {
		}

		//--------
		// Plan B. Execute a system-specific command that will print out
		// name=value lines for all environment variables.
		//--------
		osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
		if (strContains(osName, new String[]{"windows", "os/2"})) {
			//--------
			// Older versions of Windows have "command.com"
			// while newer versions of Windows (and OS/2) have
			// "cmd.exe".
			//--------
			if (strContains(osName, new String[]{"95", "98", "me", "ce"})) {
				cmd = new String[]{"command.com", "/c", "set"};
			} else {
				cmd = new String[]{"cmd.exe", "/c", "set"};
			}
		} else if (strContains(osName, "openvms")) {
			cmd = new String[]{"show", "logical"};
		} else {
			//--------
			// Hopefully, something UNIX-like
			//--------
			if (new File("/usr/bin/env").canRead()) {
				cmd = new String[]{"/usr/bin/env"};
			} else if (new File("/bin/env").canRead()) {
				cmd = new String[]{"/bin/env"};
			} else {
				cmd = new String[]{"env"};
			}
		}

		try {
			p = Runtime.getRuntime().exec(cmd);
			outThread = new EnvStreamParserThread(p.getInputStream());
			errThread = new StreamReaderThread(p.getErrorStream());
			outThread.start();
			errThread.start();
			p.waitFor();
			while (true) {
				try {
					outThread.join();
					break;
				} catch(InterruptedException ex) {
				}
			}
			while (true) {
				try {
					errThread.join();
					break;
				} catch(InterruptedException ex) {
				}
			}
			env = outThread.getEnvProperties();
		} catch(Exception ex) {
			throw new ConfigurationException("cannot get the '" + name
						+ "' environment variable because of an error "
						+ "executing '" + cmd + "': " + ex.getMessage());
		}
		return env.getProperty(name);
	}


	private boolean strContains(String str, String substring)
	{
		return str.indexOf(substring) != -1;
	}


	private boolean strContains(String str, String[] array)
	{
		int				i;

		for (i = 0; i < array.length; i++) {
			if (str.indexOf(array[i]) != -1) {
				return true;
			}
		}
		return false;
	}


	public void parse(int sourceType, String source)
						throws ConfigurationException
	{
		parse(sourceType, source, "");
	}


	public void parse(String sourceTypeAndSource) throws ConfigurationException
	{
		int					cfgSourceType;
		String				cfgSource;

		if (sourceTypeAndSource.startsWith("exec#")) {
			cfgSource = sourceTypeAndSource.substring(5);
			cfgSourceType = Configuration.INPUT_EXEC;
		} else if (sourceTypeAndSource.startsWith("file#")) {
			cfgSource = sourceTypeAndSource.substring(5);
			cfgSourceType = Configuration.INPUT_FILE;
		} else {
			cfgSource = sourceTypeAndSource;
			cfgSourceType = Configuration.INPUT_FILE;
		}
		parse(cfgSourceType, cfgSource);
	}


	public void parse(
		int					sourceType,
		String				source,
		String				sourceDescription) throws ConfigurationException
	{
		StringBuffer		trustedCmdLine;
		ConfigParser		parser;
		
		trustedCmdLine = new StringBuffer();
		switch (sourceType) {
		case Configuration.INPUT_FILE:
			this.fileName = source;
			break;
		case Configuration.INPUT_STRING:
			if (sourceDescription.equals("")) {
				this.fileName = "<string-based configuration>";
			} else {
				this.fileName = sourceDescription;
			}
			break;
		case Configuration.INPUT_EXEC:
			if (sourceDescription.equals("")) {
				this.fileName = "exec#" + source;
			} else {
				this.fileName = sourceDescription;
			}
			if (!isExecAllowed(source, trustedCmdLine)) {
				throw new ConfigurationException("cannot parse output of "
							+ "executing \"" + source + "\" due to security "
							+ "restrictions");
			}
			break;
		default:
			Util.assertion(false); // Bug!
			break;
		}
		parser = new ConfigParser(sourceType, source, trustedCmdLine.toString(),
								  this.fileName, this, false);
	}


	public String fileName()
	{
		return this.fileName;
	}


	public String[] listFullyScopedNames(
		String				scope,
		String				localName,
		int					typeMask,
		boolean				recursive) throws ConfigurationException
	{
		return listFullyScopedNames(scope, localName, typeMask, recursive,
									new String[0]);
	}


	public String[] listFullyScopedNames(
		String				scope,
		String				localName,
		int					typeMask,
		boolean				recursive,
		String				filterPattern) throws ConfigurationException
	{
		String []			filterPatterns = new String[1];

		filterPatterns[0] = filterPattern;
		return listFullyScopedNames(scope, localName, typeMask, recursive,
									filterPatterns);
	}


	public String[] listFullyScopedNames(
		String				scope,
		String				localName,
		int					typeMask,
		boolean				recursive,
		String[]			filterPatterns) throws ConfigurationException
	{
		String				fullyScopedName;
		ConfigItem			item;
		ConfigScope			scopeObj;
		String[]			result;

		fullyScopedName = mergeNames(scope, localName);
		if (fullyScopedName.equals("")) {
			scopeObj = this.rootScope;
		} else {
			item = lookup(fullyScopedName, localName, true);
			if (item == null || item.getType() != Configuration.CFG_SCOPE) {
				throw new ConfigurationException(fileName() + ": '"
										+ fullyScopedName + "' is not a scope");
			}
			scopeObj = item.getScopeVal();
		}
		result = scopeObj.listFullyScopedNames(typeMask, recursive,
											   filterPatterns);
		Arrays.sort(result);
		return result;
	}


	public String[] listLocallyScopedNames(
		String				scope,
		String				localName,
		int					typeMask,
		boolean				recursive) throws ConfigurationException
	{
		return listLocallyScopedNames(scope, localName, typeMask, recursive,
									  new String[0]);
	}


	public String[] listLocallyScopedNames(
		String				scope,
		String				localName,
		int					typeMask,
		boolean				recursive,
		String				filterPattern) throws ConfigurationException
	{
		String [] filterPatterns = new String[1];
		filterPatterns[0] = filterPattern;
		return listLocallyScopedNames(scope, localName, typeMask, recursive,
									  filterPatterns);
	}


	public String[] listLocallyScopedNames(
		String				scope,
		String				localName,
		int					typeMask,
		boolean				recursive,
		String[]			filterPatterns) throws ConfigurationException
	{
		String				fullyScopedName;
		ConfigItem			item;
		ConfigScope			scopeObj;
		String[]			result;

		fullyScopedName = mergeNames(scope, localName);
		if (fullyScopedName.equals("")) {
			scopeObj = this.rootScope;
		} else {
			item = lookup(fullyScopedName, localName, true);
			if (item == null || item.getType() != Configuration.CFG_SCOPE) {
				throw new ConfigurationException(fileName() + ": '"
										+ fullyScopedName + "' is not a scope");
			}
			scopeObj = item.getScopeVal();
		}
		result = scopeObj.listLocallyScopedNames(typeMask, recursive,
												 filterPatterns);
		Arrays.sort(result);
		return result;
	}


	public int type(String scope, String localName)
	{
		String				fullyScopedName;
		ConfigItem			item;
		int					result;
		
		fullyScopedName = mergeNames(scope, localName);
		item = lookup(fullyScopedName, localName);
		if (item == null) {
			result = Configuration.CFG_NO_VALUE;
		} else {
			result = item.getType();
		}
		return result;
	}


	public boolean uidEquals(String str1, String str2)
	{
		String				uStr1;
		String				uStr2;
		
		uStr1 = uidIdentifierProcessor.unexpand(str1);
		uStr2 = uidIdentifierProcessor.unexpand(str2);
		return uStr1.equals(uStr2);
	}


	public String expandUid(String str) throws ConfigurationException
	{
		return uidIdentifierProcessor.expand(str);
	}


	public String unexpandUid(String spelling)
						throws ConfigurationException
	{
		return uidIdentifierProcessor.unexpand(spelling);
	}


	//--------
	// Dump part or all of the configuration
	//--------
	public String dump(boolean wantExpandedUidNames)
	{
		StringBuffer		buf;

		buf = new StringBuffer();
		rootScope.dump(buf, wantExpandedUidNames);
		return buf.toString();
	}


	public String dump(
		 boolean		wantExpandedUidNames,
		 String			scope,
		 String			localName) throws ConfigurationException
	{
		ConfigItem		item;
		String			fullyScopedName;
		StringBuffer	buf;

		buf = new StringBuffer();
		fullyScopedName = mergeNames(scope, localName);
		if (fullyScopedName.equals("")) {
			rootScope.dump(buf, wantExpandedUidNames);
		} else {
			item = lookup(fullyScopedName, localName, true);
			if (item == null) {
				throw new ConfigurationException(fileName() + ": '"
									+ fullyScopedName + "' is not an entry");
			}
			item.dump(buf, fullyScopedName, wantExpandedUidNames);
		}
		return buf.toString();
	}


	static private EnumNameAndValue boolInfo[] = {
		new EnumNameAndValue("false", 0 ),
		new EnumNameAndValue("true", 1 )
	};


	public boolean isBoolean(String str)
	{
		Integer			result;

		result = enumVal(str, boolInfo);
		return (result != null);
	}


	public boolean isInt(String str)
	{
		try {
			Integer.parseInt(str);
		} catch(NumberFormatException ex) {
			return false;
		}
		return true;
	}


	public boolean isFloat(String str)
	{
		try {
			Float.parseFloat(str);
		} catch(NumberFormatException ex) {
			return false;
		}
		return true;
	}


	static private String[] allowedDurationMicrosecondsUnits = {
		"microsecond",
		"microseconds",
		"millisecond",
		"milliseconds",
		"second",
		"seconds",
		"minute",
		"minutes",
	};


	static private String[] allowedDurationMillisecondsUnits = {
		"millisecond",
		"milliseconds",
		"second",
		"seconds",
		"minute",
		"minutes",
		"hour",
		"hours",
		"day",
		"days",
		"week",
		"weeks"
	};


	static private String[] allowedDurationSecondsUnits = {
		"second",
		"seconds",
		"minute",
		"minutes",
		"hour",
		"hours",
		"day",
		"days",
		"week",
		"weeks"
	};


	public boolean isDurationMicroseconds(String str)
	{
		if (str.equals("infinite")) {
			return true;
		}
		return isFloatWithUnits(str, allowedDurationMicrosecondsUnits);
	}


	public boolean isDurationMilliseconds(String str)
	{
		if (str.equals("infinite")) {
			return true;
		}
		return isFloatWithUnits(str, allowedDurationMillisecondsUnits);
	}


	public boolean isDurationSeconds(String str)
	{
		if (str.equals("infinite")) {
			return true;
		}
		return isFloatWithUnits(str, allowedDurationSecondsUnits);
	}


	public boolean isEnum(
		String					str,
		EnumNameAndValue[]		enumInfo)
	{
		Integer					result;
		
		result = enumVal(str, enumInfo);
		if (result == null) {
			return false;
		}
		return true;
	}


	public boolean isFloatWithUnits(
		String			str,
		String[]		allowedUnits)
	{
		int				i;
		String			units;
		String			floatStr;
		int				unitsLen;
		int				strLen;
		char			ch;

		units = null;
		for (i = 0; i < allowedUnits.length; i++) {
			if (str.endsWith(allowedUnits[i])) {
				units = allowedUnits[i];
				break;
			}
		}
		if (units == null) {
			return false;
		}
		unitsLen = units.length();
		strLen = str.length();
		i = strLen - unitsLen - 1;
		if (i < 0) {
			floatStr = ""; // anything that's not a valid float
		} else {
			ch = str.charAt(i);
			while (i > 0 && (ch == ' ' || ch == '\t')) {
				i--;
				ch = str.charAt(i);
			}
			floatStr = str.substring(0, i+1);
		}
		try {
			Float.parseFloat(floatStr);
		} catch(NumberFormatException ex) {
			return false;
		}
		return true;
	}


	public boolean isIntWithUnits(
		String			str,
		String[]		allowedUnits)
	{
		int				i;
		String			units;
		String			intStr;
		int				unitsLen;
		int				strLen;
		char			ch;

		units = null;
		for (i = 0; i < allowedUnits.length; i++) {
			if (str.endsWith(allowedUnits[i])) {
				units = allowedUnits[i];
				break;
			}
		}
		if (units == null) {
			return false;
		}
		unitsLen = units.length();
		strLen = str.length();
		i = strLen - unitsLen - 1;
		if (i < 0) {
			intStr = ""; // anythign that's not a valid int
		} else {
			ch = str.charAt(i);
			while (i > 0 && (ch == ' ' || ch == '\t')) {
				i--;
				ch = str.charAt(i);
			}
			intStr = str.substring(0, i+1);
		}
		try {
			Integer.parseInt(intStr);
		} catch(NumberFormatException ex) {
			return false;
		}
		return true;
	}


	public boolean isUnitsWithFloat(
		String			str,
		String[]		allowedUnits)
	{
		int				i;
		String			units;
		String			floatStr;
		int				unitsLen;
		int				strLen;
		char			ch;

		units = null;
		for (i = 0; i < allowedUnits.length; i++) {
			if (str.startsWith(allowedUnits[i])) {
				units = allowedUnits[i];
				break;
			}
		}
		if (units == null) {
			return false;
		}
		unitsLen = units.length();
		strLen = str.length();
		if (unitsLen == strLen) {
			return false;
		}
		i = unitsLen;
		ch = str.charAt(i);
		while (i < strLen-1 && (ch == ' ' || ch == '\t')) {
			i++;
			ch = str.charAt(i);
		}
		floatStr = str.substring(i);
		try {
			Float.parseFloat(floatStr);
		} catch(NumberFormatException ex) {
			return false;
		}
		return true;
	}


	public boolean isUnitsWithInt(
		String			str,
		String[]		allowedUnits)
	{
		int				i;
		String			units;
		String			intStr;
		int				unitsLen;
		int				strLen;
		char			ch;

		units = null;
		for (i = 0; i < allowedUnits.length; i++) {
			if (str.startsWith(allowedUnits[i])) {
				units = allowedUnits[i];
				break;
			}
		}
		if (units == null) {
			return false;
		}
		unitsLen = units.length();
		strLen = str.length();
		if (unitsLen == strLen) {
			return false;
		}
		i = unitsLen;
		ch = str.charAt(i);
		while (i < strLen-1 && (ch == ' ' || ch == '\t')) {
			i++;
			ch = str.charAt(i);
		}
		intStr = str.substring(i);
		try {
			Integer.parseInt(intStr);
		} catch(NumberFormatException ex) {
			return false;
		}
		return true;
	}


	public boolean stringToBoolean(
		String			scope,
		String			localName,
		String			str) throws ConfigurationException
	{
		return stringToEnum(scope, localName, str, "boolean", boolInfo) != 0;
	}


	public int stringToInt(
		String			scope,
		String			localName,
		String			str) throws ConfigurationException
	{
		int				result;
		String			fullyScopedName;

		try {
			result = Integer.parseInt(str);
		} catch(NumberFormatException ex) {
			fullyScopedName = mergeNames(scope, localName);
			throw new ConfigurationException(fileName()
						+ ": non-integer value for '" + fullyScopedName + "'");
		}
		return result;
	}


	public float stringToFloat(
		String			scope,
		String			localName,
		String			str)
						throws ConfigurationException
	{
		float			result;
		String			fullyScopedName;

		try {
			result = Float.parseFloat(str);
		} catch(NumberFormatException ex) {
			fullyScopedName = mergeNames(scope, localName);
			throw new ConfigurationException(fileName() + ": non-numeric "
									+ "value for '" + fullyScopedName + "'");
		}
		return result;
	}


	static private ValueWithUnits durationMicrosecondsUnitsInfo[] = {
		new ValueWithUnits(1,						"microsecond"),
		new ValueWithUnits(1,						"microseconds"),
		new ValueWithUnits(1000,					"millisecond"),
		new ValueWithUnits(1000,					"milliseconds"),
		new ValueWithUnits(1000 * 1000,				"second"),
		new ValueWithUnits(1000 * 1000,				"seconds"),
		new ValueWithUnits(1000 * 1000 * 60,		"minute"),
		new ValueWithUnits(1000 * 1000 * 60,		"minutes"),
	};


	static private ValueWithUnits durationMillisecondsUnitsInfo[] = {
		new ValueWithUnits(1,						"millisecond"),
		new ValueWithUnits(1,						"milliseconds"),
		new ValueWithUnits(1000,					"second"),
		new ValueWithUnits(1000,					"seconds"),
		new ValueWithUnits(1000 * 60,				"minute"),
		new ValueWithUnits(1000 * 60,				"minutes"),
		new ValueWithUnits(1000 * 60 * 60,			"hour"),
		new ValueWithUnits(1000 * 60 * 60,			"hours"),
		new ValueWithUnits(1000 * 60 * 60 * 24,		"day"),
		new ValueWithUnits(1000 * 60 * 60 * 24,		"days"),
		new ValueWithUnits(1000 * 60 * 60 * 24 * 7,	"week"),
		new ValueWithUnits(1000 * 60 * 60 * 24 * 7,	"weeks")
	};


	static private ValueWithUnits durationSecondsUnitsInfo[] = {
		new ValueWithUnits(1,					"second"),
		new ValueWithUnits(1,					"seconds"),
		new ValueWithUnits(60,					"minute"),
		new ValueWithUnits(60,					"minutes"),
		new ValueWithUnits(60 * 60,				"hour"),
		new ValueWithUnits(60 * 60,				"hours"),
		new ValueWithUnits(60 * 60 * 24,		"day"),
		new ValueWithUnits(60 * 60 * 24,		"days"),
		new ValueWithUnits(60 * 60 * 24 * 7,	"week"),
		new ValueWithUnits(60 * 60 * 24 * 7,	"weeks")
	};


	public int stringToDurationMicroseconds(
		String				scope,
		String				localName,
		String				str) throws ConfigurationException
	{
		float				floatVal;
		String				units;
		int					i;
		int					result;
		int					unitsVal;
		ValueWithUnits		valueWithUnits;
		
		if (str.equals("infinite")) {
			return -1;
		}

		try {
			valueWithUnits = stringToFloatWithUnits(scope, localName,
											"durationMicroseconds", str,
											allowedDurationMicrosecondsUnits);
		} catch(ConfigurationException ex) {
			throw new ConfigurationException(ex.getMessage()
					+ "; alternatively, you can use 'infinite'");
		}
		units = valueWithUnits.getUnits();
		floatVal = valueWithUnits.getFloatValue();
		for (i = 0; i < durationMicrosecondsUnitsInfo.length; i++) {
			if (durationMicrosecondsUnitsInfo[i].getUnits().equals(units)) {
				break;
			}
		}
		Util.assertion(i <durationMicrosecondsUnitsInfo.length);
		unitsVal = durationMicrosecondsUnitsInfo[i].getIntValue();
		result = (int)(floatVal * unitsVal);
		return result;
	}


	public int stringToDurationMilliseconds(
		String				scope,
		String				localName,
		String				str) throws ConfigurationException
	{
		float				floatVal;
		String				units;
		int					i;
		int					result;
		int					unitsVal;
		ValueWithUnits		valueWithUnits;
		
		if (str.equals("infinite")) {
			return -1;
		}

		try {
			valueWithUnits = stringToFloatWithUnits(scope, localName,
											"durationMilliseconds", str,
											allowedDurationMillisecondsUnits);
		} catch(ConfigurationException ex) {
			throw new ConfigurationException(ex.getMessage()
					+ "; alternatively, you can use 'infinite'");
		}
		units = valueWithUnits.getUnits();
		floatVal = valueWithUnits.getFloatValue();
		for (i = 0; i < durationMillisecondsUnitsInfo.length; i++) {
			if (durationMillisecondsUnitsInfo[i].getUnits().equals(units)) {
				break;
			}
		}
		Util.assertion(i <durationMillisecondsUnitsInfo.length);
		unitsVal = durationMillisecondsUnitsInfo[i].getIntValue();
		result = (int)(floatVal * unitsVal);
		return result;
	}


	public int stringToDurationSeconds(
		String				scope,
		String				localName,
		String				str) throws ConfigurationException
	{
		float				floatVal;
		String				units;
		int					i;
		int					result;
		int					unitsVal;
		ValueWithUnits		valueWithUnits;
		
		if (str.equals("infinite")) {
			return -1;
		}

		try {
			valueWithUnits = stringToFloatWithUnits(scope, localName,
											"durationSeconds", str,
											allowedDurationSecondsUnits);
		} catch(ConfigurationException ex) {
			throw new ConfigurationException(ex.getMessage()
					+ "; alternatively, you can use 'infinite'");
		}
		units = valueWithUnits.getUnits();
		floatVal = valueWithUnits.getFloatValue();
		for (i = 0; i < durationSecondsUnitsInfo.length; i++) {
			if (durationSecondsUnitsInfo[i].getUnits().equals(units)) {
				break;
			}
		}
		Util.assertion(i <durationSecondsUnitsInfo.length);
		unitsVal = durationSecondsUnitsInfo[i].getIntValue();
		result = (int)(floatVal * unitsVal);
		return result;
	}


	static private ValueWithUnits memorySizeBytesUnitsInfo[] = {
		new ValueWithUnits(1,					"byte"),
		new ValueWithUnits(1,					"bytes"),
		new ValueWithUnits(1024,				"KB"),
		new ValueWithUnits(1024 * 1024,			"MB"),
		new ValueWithUnits(1024 * 1024 * 1024,	"GB"),
	};


	static private ValueWithUnits memorySizeKBUnitsInfo[] = {
		new ValueWithUnits(1,					"KB"),
		new ValueWithUnits(1024,				"MB"),
		new ValueWithUnits(1024 * 1024,			"GB"),
		new ValueWithUnits(1024 * 1024 * 1024,	"TB"),
	};


	static private ValueWithUnits memorySizeMBUnitsInfo[] = {
		new ValueWithUnits(1,					"MB"),
		new ValueWithUnits(1024,				"GB"),
		new ValueWithUnits(1024 * 1024,			"TB"),
		new ValueWithUnits(1024 * 1024 * 1024,	"PB"),
	};


	private int stringToMemorySizeGeneric(
		String					typeName,
		ValueWithUnits[]		unitsInfo,
		String[]				allowedSizes,
		String					scope,
		String					localName,
		String					str) throws ConfigurationException
	{
		float					floatVal;
		String					units;
		int						i;
		int						result;
		int						unitsVal;
		ValueWithUnits			valueWithUnits;
		
		valueWithUnits = stringToFloatWithUnits(scope, localName, typeName,
												str, allowedSizes);
		units = valueWithUnits.getUnits();
		floatVal = valueWithUnits.getFloatValue();
		for (i = 0; i < unitsInfo.length; i++) {
			if (unitsInfo[i].getUnits().equals(units)) {
				break;
			}
		}
		Util.assertion(i <unitsInfo.length);
		unitsVal = unitsInfo[i].getIntValue();
		result = (int)(floatVal * unitsVal);
		return result;
	}


	public boolean isMemorySizeBytes(String str)
	{
		String[]		allowedUnits = {"byte", "bytes", "KB", "MB", "GB"};

		return isFloatWithUnits(str, allowedUnits);
	}


	public boolean isMemorySizeKB(String str)
	{
		String[]		allowedUnits = {"KB", "MB", "GB", "TB"};

		return isFloatWithUnits(str, allowedUnits);
	}


	public boolean isMemorySizeMB(String str)
	{
		String[]		allowedUnits = {"MB", "GB", "TB", "PB"};

		return isFloatWithUnits(str, allowedUnits);
	}


	public int stringToMemorySizeBytes(
		String			scope,
		String			localName,
		String			str) throws ConfigurationException
	{
		String[]		allowedUnits = {"byte", "bytes", "KB", "MB", "GB"};

		return stringToMemorySizeGeneric("memorySizeBytes",
										 memorySizeBytesUnitsInfo,
										 allowedUnits, scope, localName, str);
	}


	public int stringToMemorySizeKB(
		String			scope,
		String			localName,
		String			str) throws ConfigurationException
	{
		String[]		allowedUnits = {"KB", "MB", "GB", "TB"};

		return stringToMemorySizeGeneric("memorySizeKB", memorySizeKBUnitsInfo,
										 allowedUnits, scope, localName, str);
	}

	public int stringToMemorySizeMB(
		String			scope,
		String			localName,
		String			str) throws ConfigurationException
	{
		String[]		allowedUnits = {"MB", "GB", "TB", "PB"};

		return stringToMemorySizeGeneric("memorySizeKB", memorySizeMBUnitsInfo,
										 allowedUnits, scope, localName, str);
	}


	public int lookupMemorySizeBytes(
		String			scope,
		String			localName,
		int				defaultVal) throws ConfigurationException
	{
		String			defaultStrValue;
		String			str;
		int				result;

		defaultStrValue = "" + defaultVal + " bytes";
		str = lookupString(scope, localName, defaultStrValue);
		result = stringToMemorySizeBytes(scope, localName, str);
		return result;
	}


	public int lookupMemorySizeBytes(
		String				scope,
		String				localName) throws ConfigurationException
	{
		String				str;

		str = lookupString(scope, localName);
		return stringToMemorySizeBytes(scope, localName, str);
	}


	public int lookupMemorySizeKB(
		String			scope,
		String			localName,
		int				defaultVal) throws ConfigurationException
	{
		String			defaultStrValue;
		String			str;
		int				result;

		defaultStrValue = "" + defaultVal + " KB";
		str = lookupString(scope, localName, defaultStrValue);
		result = stringToMemorySizeKB(scope, localName, str);
		return result;
	}


	public int lookupMemorySizeKB(
		String				scope,
		String				localName) throws ConfigurationException
	{
		String				str;

		str = lookupString(scope, localName);
		return stringToMemorySizeKB(scope, localName, str);
	}


	public int lookupMemorySizeMB(
		String			scope,
		String			localName,
		int				defaultVal) throws ConfigurationException
	{
		String			defaultStrValue;
		String			str;
		int				result;

		defaultStrValue = "" + defaultVal + " MB";
		str = lookupString(scope, localName, defaultStrValue);
		result = stringToMemorySizeMB(scope, localName, str);
		return result;
	}


	public int lookupMemorySizeMB(
		String				scope,
		String				localName) throws ConfigurationException
	{
		String				str;

		str = lookupString(scope, localName);
		return stringToMemorySizeMB(scope, localName, str);
	}


	public int stringToEnum(
		String					scope,
		String					localName,
		String					str,
		String					type,
		EnumNameAndValue[]		enumInfo) throws ConfigurationException
	{
		String					fullyScopedName;
		Integer					result;
		StringBuffer			msg;
		int						i;
		
		//--------
		// Check if the value matches anything in the enumInfo list.
		//--------
		result = enumVal(str, enumInfo);
		if (result == null) {
			fullyScopedName = mergeNames(scope, localName);
			msg = new StringBuffer();
			msg.append(": bad '").append(type).append("' value specified for '")
			   .append(fullyScopedName).append("': should be one of:");
			for (i = 0; i < enumInfo.length; i++) {
				msg.append(" '").append(enumInfo[i]).append("'");
				if (i < enumInfo.length-1) {
					msg.append(",");
				}
			}
			throw new ConfigurationException(msg.toString());
		}
		return result.intValue();
	}


	public ValueWithUnits stringToFloatWithUnits(
		String				scope,
		String				localName,
		String				typeName,
		String				str,
		String[]			allowedUnits) throws ConfigurationException
	{
		int					i;
		String				units;
		String				floatStr;
		String				fullyScopedName;
		StringBuffer		msg;
		int					unitsLen;
		int					strLen;
		float				floatVal;
		char				ch;
		ValueWithUnits		result;

		units = null;
		for (i = 0; i < allowedUnits.length; i++) {
			if (str.endsWith(allowedUnits[i])) {
				units = allowedUnits[i];
				break;
			}
		}
		if (units == null) {
			fullyScopedName = mergeNames(scope, localName);
			msg = new StringBuffer();
			msg.append(fileName() + ": invalid " + typeName + " ('" + str
					   + "') specified for '" + fullyScopedName
					   + "': should be '<float> <units>' where <units> are");
			for (i = 0; i < allowedUnits.length; i++) {
				msg.append(" '" + allowedUnits[i] + "'");
				if (i < allowedUnits.length-1) {
					msg.append(",");
				}
			}
			throw new ConfigurationException(msg.toString());
		}
		unitsLen = units.length();
		strLen = str.length();
		i = strLen - unitsLen - 1;
		if (i < 0) {
			floatStr = ""; // anything that's not a valid float
		} else {
			ch = str.charAt(i);
			while (i > 0 && (ch == ' ' || ch == '\t')) {
				i--;
				ch = str.charAt(i);
			}
			floatStr = str.substring(0, i+1);
		}
		try {
			floatVal = Float.parseFloat(floatStr);
		} catch(NumberFormatException ex) {
			fullyScopedName = mergeNames(scope, localName);
			msg = new StringBuffer();
			msg.append(fileName() + ": invalid " + typeName + " ('" + str
					   + "') specified for '" + fullyScopedName
					   + "': should be '<float> <units>' where <units> are");
			for (i = 0; i < allowedUnits.length; i++) {
				msg.append(" '" + allowedUnits[i] + "'");
				if (i < allowedUnits.length-1) {
					msg.append(",");
				}
			}
			throw new ConfigurationException(msg.toString());
		}
		result = new ValueWithUnits(floatVal, units);
		return result;
	}


	public ValueWithUnits stringToUnitsWithFloat(
		String				scope,
		String				localName,
		String				typeName,
		String				str,
		String[]			allowedUnits) throws ConfigurationException
	{
		int					i;
		String				units;
		String				floatStr;
		String				fullyScopedName;
		StringBuffer		msg;
		int					unitsLen;
		int					strLen;
		float				floatVal;
		char				ch;
		ValueWithUnits		result;

		units = null;
		strLen = str.length();
		for (i = 0; i < allowedUnits.length; i++) {
			if (str.startsWith(allowedUnits[i])) {
				units = allowedUnits[i];
				break;
			}
		}
		if (units == null || units.length() == strLen) {
			fullyScopedName = mergeNames(scope, localName);
			msg = new StringBuffer();
			msg.append(fileName() + ": invalid " + typeName + " ('" + str
					   + "') specified for '" + fullyScopedName
					   + "': should be '<units> <float>' where <units> are");
			for (i = 0; i < allowedUnits.length; i++) {
				msg.append(" '" + allowedUnits[i] + "'");
				if (i < allowedUnits.length-1) {
					msg.append(",");
				}
			}
			throw new ConfigurationException(msg.toString());
		}
		unitsLen = units.length();
		i = unitsLen;
		ch = str.charAt(i);
		while (i < strLen-1 && (ch == ' ' || ch == '\t')) {
			i++;
			ch = str.charAt(i);
		}
		floatStr = str.substring(i);
		try {
			floatVal = Float.parseFloat(floatStr);
		} catch(NumberFormatException ex) {
			fullyScopedName = mergeNames(scope, localName);
			msg = new StringBuffer();
			msg.append(fileName() + ": invalid " + typeName + " ('" + str
					   + "') specified for '" + fullyScopedName
					   + "': should be '<units> <float>' where <units> are");
			for (i = 0; i < allowedUnits.length; i++) {
				msg.append(" '" + allowedUnits[i] + "'");
				if (i < allowedUnits.length-1) {
					msg.append(",");
				}
			}
			throw new ConfigurationException(msg.toString());
		}
		result = new ValueWithUnits(floatVal, units);
		return result;
	}


	public ValueWithUnits stringToIntWithUnits(
		String				scope,
		String				localName,
		String				typeName,
		String				str,
		String[]			allowedUnits) throws ConfigurationException
	{
		int					i;
		String				units;
		String				intStr;
		String				fullyScopedName;
		StringBuffer		msg;
		int					unitsLen;
		int					strLen;
		int					intVal;
		char				ch;
		ValueWithUnits		result;

		units = null;
		for (i = 0; i < allowedUnits.length; i++) {
			if (str.endsWith(allowedUnits[i])) {
				units = allowedUnits[i];
				break;
			}
		}
		if (units == null) {
			fullyScopedName = mergeNames(scope, localName);
			msg = new StringBuffer();
			msg.append(fileName() + ": invalid " + typeName + " ('" + str
					   + "') specified for '" + fullyScopedName
					   + "': should be '<float> <units>' where <units> are");
			for (i = 0; i < allowedUnits.length; i++) {
				msg.append(" '" + allowedUnits[i] + "'");
				if (i < allowedUnits.length-1) {
					msg.append(",");
				}
			}
			throw new ConfigurationException(msg.toString());

		}
		unitsLen = units.length();
		strLen = str.length();
		i = strLen - unitsLen - 1;
		if (i < 0) {
			intStr = ""; // anything that is not a valid int
		} else {
			ch = str.charAt(i);
			while (i > 0 && (ch == ' ' || ch == '\t')) {
				i--;
				ch = str.charAt(i);
			}
			intStr = str.substring(0, i-1);
		}
		try {
			intVal = Integer.parseInt(intStr);
		} catch(NumberFormatException ex) {
			fullyScopedName = mergeNames(scope, localName);
			msg = new StringBuffer();
			msg.append(fileName() + ": invalid " + typeName + " ('" + str
					   + "') specified for '" + fullyScopedName
					   + "': should be '<float> <units>' where <units> are");
			for (i = 0; i < allowedUnits.length; i++) {
				msg.append(" '" + allowedUnits[i] + "'");
				if (i < allowedUnits.length-1) {
					msg.append(",");
				}
			}
			throw new ConfigurationException(msg.toString());
		}
		result = new ValueWithUnits(intVal, units);
		return result;
	}


	public ValueWithUnits stringToUnitsWithInt(
		String				scope,
		String				localName,
		String				typeName,
		String				str,
		String[]			allowedUnits) throws ConfigurationException
	{
		int					i;
		String				units;
		String				intStr;
		String				fullyScopedName;
		StringBuffer		msg;
		int					unitsLen;
		int					strLen;
		int					intVal;
		char				ch;
		ValueWithUnits		result;

		units = null;
		strLen = str.length();
		for (i = 0; i < allowedUnits.length; i++) {
			if (str.startsWith(allowedUnits[i])) {
				units = allowedUnits[i];
				break;
			}
		}
		if (units == null || units.length() == strLen) {
			fullyScopedName = mergeNames(scope, localName);
			msg = new StringBuffer();
			msg.append(fileName() + ": invalid " + typeName + " ('" + str
					   + "') specified for '" + fullyScopedName
					   + "': should be '<units> <int>' where <units> are");
			for (i = 0; i < allowedUnits.length; i++) {
				msg.append(" '" + allowedUnits[i] + "'");
				if (i < allowedUnits.length-1) {
					msg.append(",");
				}
			}
			throw new ConfigurationException(msg.toString());
		}
		unitsLen = units.length();
		i = unitsLen;
		ch = str.charAt(i);
		while (i < strLen-1 && (ch == ' ' || ch == '\t')) {
			i++;
			ch = str.charAt(i);
		}
		intStr = str.substring(i);
		try {
			intVal = Integer.parseInt(intStr);
		} catch(NumberFormatException ex) {
			fullyScopedName = mergeNames(scope, localName);
			msg = new StringBuffer();
			msg.append(fileName() + ": invalid " + typeName + " ('" + str
					   + "') specified for '" + fullyScopedName
					   + "': should be '<units> <int>' where <units> are");
			for (i = 0; i < allowedUnits.length; i++) {
				msg.append(" '" + allowedUnits[i] + "'");
				if (i < allowedUnits.length-1) {
					msg.append(",");
				}
			}
			throw new ConfigurationException(msg.toString());
		}
		result = new ValueWithUnits(intVal, units);
		return result;
	}


	//--------
	// lookup<Type>() operations, with and without default values.
	//--------
	public String lookupString(
		String				scope,
		String				localName,
		String				defaultVal) throws ConfigurationException
	{
		int			 		type;
		StringBuffer		str;
		String				fullyScopedName;
		
		fullyScopedName = mergeNames(scope, localName);
		str = new StringBuffer();
		type = stringValueAndType(fullyScopedName, localName, str);
		switch (type) {
		case Configuration.CFG_STRING:
			break;
		case Configuration.CFG_NO_VALUE:
			str.append(defaultVal);
			break;
		case Configuration.CFG_SCOPE:
			throw new ConfigurationException(fileName() + ": '"
										+ fullyScopedName
										+ "' is a scope instead of a string");
		case Configuration.CFG_LIST:
			throw new ConfigurationException(fileName() + ": '"
										+ fullyScopedName
										+ "' is a list instead of a string");
		default:
			Util.assertion(false); // Bug
		}
		return str.toString();
	}


	public String lookupString(String scope, String localName)
												throws ConfigurationException
	{
		int			 		type;
		StringBuffer		str;
		String				fullyScopedName;
		
		fullyScopedName = mergeNames(scope, localName);
		str = new StringBuffer();
		type = stringValueAndType(fullyScopedName, localName, str);
		switch (type) {
		case Configuration.CFG_STRING:
			break;
		case Configuration.CFG_NO_VALUE:
			throw new ConfigurationException(fileName() + ": no value "
								+ "specified for '" + fullyScopedName + "'");
		case Configuration.CFG_SCOPE:
			throw new ConfigurationException(fileName() + ": '"
								+ fullyScopedName
								+ "' is a scope instead of a string");
		case Configuration.CFG_LIST:
			throw new ConfigurationException(fileName() + ": '"
								+ fullyScopedName
								+ "' is a list instead of a string");
		default:
			Util.assertion(false); // Bug
		}
		return str.toString();
	}


	public String[] lookupList(
		String			scope,
		String			localName,
		String[]		defaultArray) throws ConfigurationException
	{
		int		 			type;
		String				fullyScopedName;
		ArrayList<String>	result;
		
		fullyScopedName = mergeNames(scope, localName);
		result = new ArrayList<String>();
		type = listValueAndType(fullyScopedName, localName, result);
		switch (type) {
		case Configuration.CFG_LIST:
			return (String[])result.toArray(new String[result.size()]);
		case Configuration.CFG_NO_VALUE:
			return defaultArray;
		case Configuration.CFG_SCOPE:
			throw new ConfigurationException(fileName() + ": '"
										+ fullyScopedName
										+ "' is a scope instead of a list");
		case Configuration.CFG_STRING:
			throw new ConfigurationException(fileName() + ": '"
										+ fullyScopedName
										+ "' is a string instead of a list");
		default:
			Util.assertion(false); // Bug
			return null; // keep the compiler happy
		}
	}


	public String[] lookupList(String scope, String localName)
												throws ConfigurationException
	{
		int			 		type;
		String				fullyScopedName;
		ArrayList<String>	result;
		
		fullyScopedName = mergeNames(scope, localName);
		result = new ArrayList<String>();
		type = listValueAndType(fullyScopedName, localName, result);
		switch (type) {
		case Configuration.CFG_LIST:
			return (String[])result.toArray(new String[result.size()]);
		case Configuration.CFG_NO_VALUE:
			throw new ConfigurationException(fileName()
										+ ": no value specified for '"
										+ fullyScopedName + "'");
		case Configuration.CFG_SCOPE:
			throw new ConfigurationException(fileName() + ": '"
										+ fullyScopedName
										+ "' is a scope instead of a list");
		case Configuration.CFG_STRING:
			throw new ConfigurationException(fileName() + ": '"
										+ fullyScopedName
										+ "' is a string instead of a list");
		default:
			Util.assertion(false); // Bug
			return null; // keep the compiler happy
		}
	}


	public int lookupInt(
		String				scope,
		String				localName,
		int					defaultVal) throws ConfigurationException
	{
		String				strValue;
		int					result;
		String				defaultStrVal;

		defaultStrVal = "" + defaultVal;
		strValue = lookupString(scope, localName, defaultStrVal);
		result = stringToInt(scope, localName, strValue);
		return result;
	}


	public int lookupInt(String scope, String localName)
												throws ConfigurationException
	{
		String				strValue;
		int					result;

		strValue = lookupString(scope, localName);
		result = stringToInt(scope, localName, strValue);
		return result;
	}


	public float lookupFloat(
		String				scope,
		String				localName,
		float				defaultVal) throws ConfigurationException
	{
		String				strValue;
		float				result;
		String				defaultStrVal;

		defaultStrVal = "" + defaultVal;
		strValue = lookupString(scope, localName, defaultStrVal);
		result = stringToFloat(scope, localName, strValue);
		return result;
	}


	public float lookupFloat(String scope, String localName)
												throws ConfigurationException
	{
		String				strValue;
		float				result;

		strValue = lookupString(scope, localName);
		result = stringToFloat(scope, localName, strValue);
		return result;
	}


	public int lookupEnum(
		String					scope,
		String					localName,
		String					typeName,
		EnumNameAndValue[]		enumInfo,
		String					defaultVal) throws ConfigurationException
	{
		StringBuffer			msg;
		String					strValue;
		String					fullyScopedName;
		Integer					result;
		int						i;

		strValue = lookupString(scope, localName, defaultVal);
		result = enumVal(strValue, enumInfo);
		if (result == null) {
			msg = new StringBuffer();
			fullyScopedName = mergeNames(scope, localName);
			msg.append(fileName() + ": bad " + typeName + " value ('"
					   + strValue + "') specified for '" + fullyScopedName
					   + "; should be one of:");
			for (i = 0; i < enumInfo.length; i++) {
				if (i < enumInfo.length) {
					msg.append(" '" + enumInfo[i].getName() + "',");
				} else {
					msg.append(" '" + enumInfo[i].getName() + "'");
				}
			}
			throw new ConfigurationException(msg.toString());
		}
		return result.intValue();
	}


	public int lookupEnum(
		String					scope,
		String					localName,
		String					typeName,
		EnumNameAndValue[]		enumInfo,
		int						defaultVal) throws ConfigurationException
	{
		StringBuffer			msg;
		String					strValue;
		String					fullyScopedName;
		Integer					result;
		int						i;

		if (type(scope, localName) == Configuration.CFG_NO_VALUE) {
			return defaultVal;
		}

		strValue = lookupString(scope, localName);
		result = enumVal(strValue, enumInfo);
		if (result == null) {
			msg = new StringBuffer();
			fullyScopedName = mergeNames(scope, localName);
			msg.append(fileName() + ": bad " + typeName + " value ('"
					   + strValue + "') specified for '" + fullyScopedName
					   + "; should be one of:");
			for (i = 0; i < enumInfo.length; i++) {
				if (i < enumInfo.length) {
					msg.append(" '" + enumInfo[i].getName() + "',");
				} else {
					msg.append(" '" + enumInfo[i].getName() + "'");
				}
			}
			throw new ConfigurationException(msg.toString());
		}
		return result.intValue();
	}


	public int lookupEnum(
		String					scope,
		String					localName,
		String					typeName,
		EnumNameAndValue[]		enumInfo) throws ConfigurationException
	{
		StringBuffer			msg;
		String					strValue;
		String					fullyScopedName;
		Integer					result;
		int						i;

		strValue = lookupString(scope, localName);
		result = enumVal(strValue, enumInfo);
		if (result == null) {
			msg = new StringBuffer();
			fullyScopedName = mergeNames(scope, localName);
			msg.append(fileName() + ": bad " + typeName + " value ('"
					   + strValue + "') specified for '" + fullyScopedName
					   + "; should be one of:");
			for (i = 0; i < enumInfo.length; i++) {
				if (i < enumInfo.length) {
					msg.append(" '" + enumInfo[i].getName() + "',");
				} else {
					msg.append(" '" + enumInfo[i].getName() + "'");
				}
			}
			throw new ConfigurationException(msg.toString());
		}
		return result.intValue();
	}


	public boolean lookupBoolean(
		String				scope,
		String				localName,
		boolean				defaultVal) throws ConfigurationException
	{
		int					result;
		String				defaultStrVal;

		if (defaultVal) {
			defaultStrVal = "true";
		} else {
			defaultStrVal = "false";
		}
		result = lookupEnum(scope, localName, "boolean", boolInfo,
							defaultStrVal);
		return result != 0;
	}


	public boolean lookupBoolean(String scope, String localName)
												throws ConfigurationException
	{
		return lookupEnum(scope, localName, "boolean", boolInfo) != 0;
	}


	public ValueWithUnits lookupFloatWithUnits(
		String				scope,
		String				localName,
		String				typeName,
		String[]			allowedUnits) throws ConfigurationException
	{
		String				str;
		ValueWithUnits		result;

		str = lookupString(scope, localName);
		result = stringToFloatWithUnits(scope, localName, typeName,
							str, allowedUnits);
		return result;
	}


	public ValueWithUnits lookupFloatWithUnits(
		String				scope,
		String				localName,
		String				typeName,
		String[]			allowedUnits,
		ValueWithUnits		defaultValueWithUnits) throws ConfigurationException
	{
		String				str;
		ValueWithUnits		result;

		if (type(scope, localName) == Configuration.CFG_NO_VALUE) {
			result = defaultValueWithUnits;
		} else {
			str = lookupString(scope, localName);
			result = stringToFloatWithUnits(scope, localName,
						typeName, str, allowedUnits);
		}
		return result;
	}


	public ValueWithUnits lookupUnitsWithFloat(
		String				scope,
		String				localName,
		String				typeName,
		String[]			allowedUnits) throws ConfigurationException
	{
		String				str;

		str = lookupString(scope, localName);
		return stringToUnitsWithFloat(scope, localName, typeName,
							str, allowedUnits);
	}


	public ValueWithUnits lookupUnitsWithFloat(
		String				scope,
		String				localName,
		String				typeName,
		String[]			allowedUnits,
		ValueWithUnits		defaultValueWithUnits) throws ConfigurationException
	{
		String				str;

		if (type(scope, localName) == Configuration.CFG_NO_VALUE) {
			return defaultValueWithUnits;
		} else {
			str = lookupString(scope, localName);
			return stringToUnitsWithFloat(scope, localName,
						typeName, str, allowedUnits);
		}
	}


	public ValueWithUnits lookupIntWithUnits(
		String				scope,
		String				localName,
		String				typeName,
		String[]			allowedUnits) throws ConfigurationException
	{
		String				str;
		ValueWithUnits		result;

		str = lookupString(scope, localName);
		result = stringToIntWithUnits(scope, localName, typeName,
							str, allowedUnits);
		return result;
	}


	public ValueWithUnits lookupIntWithUnits(
		String				scope,
		String				localName,
		String				typeName,
		String[]			allowedUnits,
		ValueWithUnits		defaultValueWithUnits) throws ConfigurationException
	{
		String				str;

		if (type(scope, localName) == Configuration.CFG_NO_VALUE) {
			return defaultValueWithUnits;
		} else {
			str = lookupString(scope, localName);
			return stringToIntWithUnits(scope, localName, typeName, str,
										allowedUnits);
		}
	}


	public ValueWithUnits lookupUnitsWithInt(
		String				scope,
		String				localName,
		String				typeName,
		String[]			allowedUnits) throws ConfigurationException
	{
		String				str;

		str = lookupString(scope, localName);
		return stringToUnitsWithInt(scope, localName, typeName, str,
									allowedUnits);
	}


	public ValueWithUnits lookupUnitsWithInt(
		String				scope,
		String				localName,
		String				typeName,
		String[]			allowedUnits,
		ValueWithUnits		defaultValueWithUnits) throws ConfigurationException
	{
		String				str;

		if (type(scope, localName) == Configuration.CFG_NO_VALUE) {
			return defaultValueWithUnits;
		} else {
			str = lookupString(scope, localName);
			return stringToUnitsWithInt(scope, localName, typeName, str,
										allowedUnits);
		}
	}


	public int lookupDurationMicroseconds(
		String				scope,
		String				localName,
		int					defaultVal) throws ConfigurationException
	{
		String				defaultStrValue;
		String				str;

		if (defaultVal == -1) {
			defaultStrValue = "infinite";
		} else {
			defaultStrValue = "" + defaultVal + " microseconds";
		}
		str = lookupString(scope, localName, defaultStrValue);
		return stringToDurationMicroseconds(scope, localName, str);
	}


	public int lookupDurationMicroseconds(
		String				scope,
		String				localName) throws ConfigurationException
	{
		String				str;

		str = lookupString(scope, localName);
		return stringToDurationMicroseconds(scope, localName, str);
	}


	public int lookupDurationMilliseconds(
		String				scope,
		String				localName,
		int					defaultVal) throws ConfigurationException
	{
		String				defaultStrValue;
		String				str;

		if (defaultVal == -1) {
			defaultStrValue = "infinite";
		} else {
			defaultStrValue = "" + defaultVal + " milliseconds";
		}
		str = lookupString(scope, localName, defaultStrValue);
		return stringToDurationMilliseconds(scope, localName, str);
	}


	public int lookupDurationMilliseconds(
		String				scope,
		String				localName) throws ConfigurationException
	{
		String				str;

		str = lookupString(scope, localName);
		return stringToDurationMilliseconds(scope, localName, str);
	}


	public int lookupDurationSeconds(
		String				scope,
		String				localName,
		int					defaultVal) throws ConfigurationException
	{
		String				defaultStrValue;
		String				str;

		if (defaultVal == -1) {
			defaultStrValue = "infinite";
		} else {
			defaultStrValue = "" + defaultVal + " seconds";
		}
		str = lookupString(scope, localName, defaultStrValue);
		return stringToDurationSeconds(scope, localName, str);
	}


	public int lookupDurationSeconds(
		String				scope,
		String				localName) throws ConfigurationException
	{
		String				str;

		str = lookupString(scope, localName);
		return stringToDurationSeconds(scope, localName, str);
	}


	public void lookupScope(
		String				scope,
		String				localName) throws ConfigurationException
	{
		String				fullyScopedName;

		fullyScopedName = mergeNames(scope, localName);
		switch (type(scope, localName)) {
		case Configuration.CFG_SCOPE:
			// Okay
			break;
		case Configuration.CFG_STRING:
			throw new ConfigurationException(fileName() + ": '"
										+ fullyScopedName
										+ "' is a string instead of a scope");
		case Configuration.CFG_LIST:
			throw new ConfigurationException(fileName() + ": '"
										+ fullyScopedName
										+ "' is a list instead of a scope");
		case Configuration.CFG_NO_VALUE:
			throw new ConfigurationException(fileName()
										+ ": scope '" + fullyScopedName
										+ "' does not exist");
		default:
			Util.assertion(false); // Bug!
		}
	}


	//--------
	// Update operations
	//--------

	public void	insertString(
		String				scope,
		String				localName,
		String				strValue) throws ConfigurationException
	{
		String[]			array;
		int					len;
		ConfigScope			scopeObj;
		String				fullyScopedName;

		fullyScopedName = mergeNames(scope, localName);
		array = Util.splitScopedNameIntoArray(fullyScopedName);
		len = array.length;
		scopeObj = ensureScopeExists(array, 0, len-2);
		if (!scopeObj.addOrReplaceString(array[len-1], strValue)) {
			throw new ConfigurationException(fileName()
										+ ": variable '" + fullyScopedName
										+ "' was previously used as a scope");
		}
	}


	public void insertList(
		String				scope,
		String				localName,
		String[]			listValue) throws ConfigurationException
	{
		String[]			array;
		int					len;
		ConfigScope			scopeObj;
		String				fullyScopedName;

		fullyScopedName = mergeNames(scope, localName);
		array = Util.splitScopedNameIntoArray(fullyScopedName);
		len = array.length;
		scopeObj = ensureScopeExists(array, 0, len-2);
		if (!scopeObj.addOrReplaceList(array[len-1], listValue)) {
			throw new ConfigurationException(fileName()
										+ ": variable '" + fullyScopedName
										+ "' was previously used as a scope");
		}
	}


	public void insertList(
		String				scope,
		String				localName,
		ArrayList<String>	listValue) throws ConfigurationException
	{
		String[]			array;
		int					len;
		ConfigScope			scopeObj;
		String				fullyScopedName;

		fullyScopedName = mergeNames(scope, localName);
		array = Util.splitScopedNameIntoArray(fullyScopedName);
		len = array.length;
		scopeObj = ensureScopeExists(array, 0, len-2);
		if (!scopeObj.addOrReplaceList(array[len-1], listValue)) {
			throw new ConfigurationException(fileName()
										+ ": variable '" + fullyScopedName
										+ "' was previously used as a scope");
		}
	}


	public void remove(String scope, String localName)
												throws ConfigurationException
	{
		String				fullyScopedName;
		String[]			array;
		int					i;
		ConfigScope			scopeObj;
		ConfigItem			item;

		scopeObj = this.currScope;
		fullyScopedName = mergeNames(scope, localName);
		array = Util.splitScopedNameIntoArray(fullyScopedName);
		for (i = 0; i < array.length-1; i++) {
			item = scopeObj.findItem(array[i]);
			if (item == null || item.getType() != Configuration.CFG_SCOPE) {
				throw new ConfigurationException(fileName() + ": '"
									+ fullyScopedName + "' does not exist");
			}
			scopeObj = item.getScopeVal();
			Util.assertion(scopeObj != null);
		}
		Util.assertion(i == array.length-1);
		Util.assertion(scopeObj != null);
		if (!scopeObj.removeItem(array[i])) {
			throw new ConfigurationException(fileName() + ": '"
									+ fullyScopedName + "' does not exist");
		}
	}


	public void empty()
	{
		fileName      = "<no file>";
		rootScope     = new ConfigScope(null, "");
		currScope     = rootScope;
		fileNameStack = new ArrayList<String>();
	}


	//--------
	// Operations called by ConfigParser
	//--------

	void insertList(String name, String[] listValue)
												throws ConfigurationException
	{
		String[]			array;
		int					len;
		ConfigScope			scopeObj;

		array = Util.splitScopedNameIntoArray(name);
		len = array.length;
		scopeObj = ensureScopeExists(array, 0, len-2);
		if (!scopeObj.addOrReplaceList(array[len-1], listValue)) {
			throw new ConfigurationException(fileName() + ": variable '"
								+ name + "' was previously used as a scope");
		}
	}


	void insertList(String name, ArrayList<String> listValue)
												throws ConfigurationException
	{
		String[]			array;
		int					len;
		ConfigScope			scopeObj;

		array = Util.splitScopedNameIntoArray(name);
		len = array.length;
		scopeObj = ensureScopeExists(array, 0, len-2);
		if (!scopeObj.addOrReplaceList(array[len-1], listValue)) {
			throw new ConfigurationException(fileName() + ": variable '" + name
										+ "' was previously used as a scope");
		}
	}


	ConfigScope getRootScope()
	{
		return rootScope;
	}


	ConfigScope getCurrScope()
	{
		return currScope;
	}


	void setCurrScope(ConfigScope scope)
	{
		currScope = scope;
	}


	public void	ensureScopeExists(
			String			scope,
			String			localName) throws ConfigurationException
	{
		String				fullyScopedName;

		fullyScopedName = mergeNames(scope, localName);
		ensureScopeExists(fullyScopedName);
	}


	ConfigScope ensureScopeExists(String fullyScopedName)
						throws ConfigurationException
	{
		String[]			array;

		array = Util.splitScopedNameIntoArray(fullyScopedName);
		return ensureScopeExists(array, 0, array.length-1);
	}


	ConfigScope ensureScopeExists(
		String[]		array,
		int				firstIndex,
		int				lastIndex) throws ConfigurationException
	{
		int				i;
		int				j;
		ConfigScope		scope;
		StringBuffer	msg;

		scope = this.currScope;
		for (i = firstIndex; i <= lastIndex; i++) {
			scope = scope.ensureScopeExists(array[i]);
			if (scope == null) {
				msg = new StringBuffer();
				msg.append(fileName() + ": scope '");
				for (j = firstIndex; j <= i; j++) {
					msg.append(array[j]);
					if (j < i) {
						msg.append(".");
					}
				}
				msg.append("' was previously used as a variable name");
				throw new ConfigurationException(msg.toString());
			}
		}
		Util.assertion(scope != null);
		return scope;
	}


	boolean isExecAllowed(String cmdLine, StringBuffer trustedCmdLine)
	{
		String[]				allowPatterns;
		String[]				denyPatterns;
		String[]				trustedDirs;
		StringBuffer			cmd;
		String					scope;
		String					dirSeparator;
		int						i;
		int						j;
		int						len;
		char					ch;

		if (this == DefaultSecurityConfiguration.singleton
		    || securityCfg == null)
		{
			return false;
		}
		dirSeparator = System.getProperty("file.separator");
		scope = securityCfgScope;

		allowPatterns = securityCfg.lookupList(scope, "allow_patterns");
		denyPatterns  = securityCfg.lookupList(scope, "deny_patterns");
		trustedDirs   = securityCfg.lookupList(scope, "trusted_directories");

		//--------
		// check if there is any rule to deny execution
		//--------
		for (i = 0; i < denyPatterns.length; i++) {
			if (patternMatch(cmdLine, denyPatterns[i])) {
				return false;
			}
		}

		//--------
		// Check if tehre is any rule to allow execution AND the
		// command can be found in trusted_directories.
		//--------
		for (i = 0; i < allowPatterns.length; i++) {
			if (!patternMatch(cmdLine, allowPatterns[i])) {
				continue;
			}
			//--------
			// Found cmdLine in allow_patterns. Now extract
			// the first word from cmdLine to get the actual
			// command.
			//--------
			cmd = new StringBuffer();
			len = cmdLine.length();
			for (j = 0; j < len; j++) {
				ch = cmdLine.charAt(j);
				if (Character.isWhitespace(ch)) {
					break;
				}
				cmd.append(ch);
			}

			//--------
			// Check if cmd resides in a directory in
			// trusted_directories.
			//--------
			for (j = 0; j < trustedDirs.length; j++) {
				if (Util.isCmdInDir(cmd.toString(), trustedDirs[j])) {
					trustedCmdLine.delete(0, trustedCmdLine.length());
					trustedCmdLine.append(trustedDirs[j] + dirSeparator
																  + cmdLine);
					return true;
				}
			}
		}
		return false;
	}


	//--------
	// Helper operations
	//--------

	ConfigItem lookup(String fullyScopedName, String localName)
	{
		return lookup(fullyScopedName, localName, false);
	}


	ConfigItem lookup(
		String				fullyScopedName,
		String				localName,
		boolean				startInRoot)
	{
		String[]			array;
		ConfigScope			scope;
		ConfigItem			item;

		if (fullyScopedName.equals("")) {
			return null;
		}
		if (fullyScopedName.startsWith(".")) {
			//--------
			// Search only in the root scope and skip over "."
			//--------
			array = Util.splitScopedNameIntoArray(fullyScopedName.substring(1));
			scope = rootScope;
		} else if (startInRoot) {
			//--------
			// Search only in the root scope
			//--------
			array = Util.splitScopedNameIntoArray(fullyScopedName);
			scope = rootScope;
		} else {
			//--------
			// Start search from the current scope
			//--------
			array = Util.splitScopedNameIntoArray(fullyScopedName);
			scope = currScope;
		}
		item = null;
		while (scope != null) {
			item = lookupHelper(scope, array);
			if (item != null) {
				break;
			}
			scope = scope.getParentScope();
		}
		if (item == null && fallbackCfg != null) {
			item = fallbackCfg.lookup(localName, localName, true);
		}
		return item;
	}


	ConfigItem lookupHelper(ConfigScope scope, String[] array)
	{
		int				i;
		int				len;
		ConfigItem		item;

		len = array.length;
		for (i = 0; i < len - 1; i++) {
			item = scope.findItem(array[i]);
			if (item == null || item.getType() != Configuration.CFG_SCOPE) {
				return null;
			}
			scope = item.getScopeVal();
			Util.assertion(scope != null);
		}
		Util.assertion(i == len -1);
		Util.assertion(scope != null);
		item = scope.findItem(array[i]);
		return item;
	}


	int stringValueAndType(
		String				fullyScopedName,
		String				localName,
		StringBuffer		str)
	{
		ConfigItem			item;
		int					type;

		str.delete(0, str.length());
		item = lookup(fullyScopedName, localName);
		if (item == null) {
			type = Configuration.CFG_NO_VALUE;
		} else {
			type = item.getType();
			if (type == Configuration.CFG_STRING) {
				str.append(item.getStringVal());
			}
		}
		return type;
	}


	int listValueAndType(
		String				fullyScopedName,
		String				localName,
		ArrayList<String>	list)
	{
		ConfigItem			item;
		String[]			array;
		int					type;
		int					i;

		list.clear();
		item = lookup(fullyScopedName, localName);
		if (item == null) {
			type = Configuration.CFG_NO_VALUE;
		} else {
			type = item.getType();
			if (type == Configuration.CFG_LIST) {
				array = item.getListVal();
				for (i = 0; i < array.length; i++) {
					list.add(array[i]);
				}
			}
		}
		return type;
	}


	void pushIncludedFilename(String fileName)
	{
		fileNameStack.add(fileName);
	}


	void popIncludedFilename(String fileName)
	{
		String			str;
		int				size;

		size = fileNameStack.size();
		Util.assertion(size > 0);
		str = (String)fileNameStack.get(size-1);
		Util.assertion(fileName.equals(str));
		fileNameStack.remove(size-1);
	}


	void checkForCircularIncludes(String fileName, int includeLineNum)
												throws ConfigurationException
	{
		String			str;
		int				size;
		int				i;

		size = fileNameStack.size();
		for (i = 0; i < size; i++) {
			str = (String)fileNameStack.get(i);
			if (fileName.equals(str)) {
				throw new ConfigurationException( fileName() + ": line "
								+ includeLineNum + ", circular include of '"
								+ fileName + "'");
			}
		}
	}


	UidIdentifierProcessor		uidIdentifierProcessor;
	Configuration				securityCfg;
	String						securityCfgScope;
	String						fileName;
	ConfigScope					rootScope;
	ConfigScope					currScope;
	ArrayList<String>			fileNameStack;
	ConfigurationImpl			fallbackCfg;
	Properties					env;
}
