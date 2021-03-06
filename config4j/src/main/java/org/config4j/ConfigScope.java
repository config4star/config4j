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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

class ConfigScope {
	private final ConfigScope parentScope;

	private String scopedName;

	private final Map<String, ConfigItem> table;

	ConfigScope(ConfigScope parentScope, String name) {
		table = new HashMap<String, ConfigItem>(16);
		this.parentScope = parentScope;
		if (parentScope == null) {
			Util.assertion(name.equals(""));
			scopedName = "";
		} else if (parentScope.parentScope == null) {
			scopedName = name;
		} else {
			scopedName = parentScope.scopedName + "." + name;
		}
	}

	boolean addOrReplaceList(String name, ArrayList<String> listVal) {
		ConfigItem item;
		String[] array;

		item = table.get(name);
		if (item != null && item.getType() == Configuration.CFG_SCOPE) {
			// --------
			// Fail because there is a scope with the same name
			// --------
			return false;
		} else {
			array = new String[listVal.size()];
			listVal.toArray(array);
			table.put(name, new ConfigItem(name, array));
			return true;
		}
	}

	boolean addOrReplaceList(String name, String[] listVal) {
		ConfigItem item;

		item = table.get(name);
		if (item != null && item.getType() == Configuration.CFG_SCOPE) {
			// --------
			// Fail because there is a scope with the same name
			// --------
			return false;
		} else {
			table.put(name, new ConfigItem(name, listVal));
			return true;
		}
	}

	boolean addOrReplaceString(String name, String strVal) {
		ConfigItem item;

		item = table.get(name);
		if (item != null && item.getType() == Configuration.CFG_SCOPE) {
			// --------
			// Fail because there is a scope with the same name
			// --------
			return false;
		} else {
			table.put(name, new ConfigItem(name, strVal));
			return true;
		}
	}

	void dump(StringBuffer buf, boolean wantExpandedUidNames) {
		dump(buf, wantExpandedUidNames, 0);
	}

	void dump(StringBuffer buf, boolean wantExpandedUidNames, int indentLevel) {
		String[] namesArray;
		int i;
		ConfigItem item;

		// --------
		// First pass. Dump the variables.
		// --------
		namesArray = listLocalNames(Configuration.CFG_VARIABLES);
		Arrays.sort(namesArray);
		for (i = 0; i < namesArray.length; i++) {
			item = table.get(namesArray[i]);
			Util.assertion(item != null);
			Util.assertion((item.getType() & Configuration.CFG_VARIABLES) != 0);
			item.dump(buf, namesArray[i], wantExpandedUidNames, indentLevel);
		}

		// --------
		// Second pass. Dump the nested scopes.
		// --------
		namesArray = listLocalNames(Configuration.CFG_SCOPE);
		Arrays.sort(namesArray);
		for (i = 0; i < namesArray.length; i++) {
			item = table.get(namesArray[i]);
			Util.assertion((item.getType() & Configuration.CFG_SCOPE) != 0);
			item.dump(buf, namesArray[i], wantExpandedUidNames, indentLevel);
		}
	}

	ConfigScope ensureScopeExists(String name) {
		ConfigItem item;
		ConfigScope scope;

		item = table.get(name);
		if (item != null && item.getType() != Configuration.CFG_SCOPE) {
			// --------
			// Fail because it already exists, but not as a scope
			// --------
			scope = null;
		} else if (item != null) {
			// --------
			// It already exists.
			// --------
			scope = item.getScopeVal();
		} else {
			// --------
			// It doesn't already exist. Create it.
			// --------
			scope = new ConfigScope(this, name);
			table.put(name, new ConfigItem(name, scope));
		}
		return scope;
	}

	ConfigItem findItem(String name) {
		return table.get(name);
	}

	ConfigScope getParentScope() {
		return parentScope;
	}

	String getScopedName() {
		return scopedName;
	}

	private boolean listFilter(String name, String[] filterPatterns) {
		int i;
		String unexpandedName;
		String pattern;
		UidIdentifierProcessor uidProc;

		if (filterPatterns.length == 0) {
			return true;
		}

		uidProc = new UidIdentifierProcessor();
		unexpandedName = uidProc.unexpand(name);
		for (i = 0; i < filterPatterns.length; i++) {
			pattern = filterPatterns[i];
			if (Configuration.patternMatch(unexpandedName, pattern)) {
				return true;
			}
		}
		return false;
	}

	String[] listFullyScopedNames(int typeMask, boolean recursive) {
		ArrayList<String> vec = new ArrayList<String>();
		String[] result;

		listScopedNamesHelper(scopedName, typeMask, recursive, new String[0], vec);
		result = new String[vec.size()];
		return vec.toArray(result);
	}

	String[] listFullyScopedNames(int typeMask, boolean recursive, String[] filterPatterns) {
		ArrayList<String> vec = new ArrayList<String>();
		String[] result;

		listScopedNamesHelper(scopedName, typeMask, recursive, filterPatterns, vec);
		result = new String[vec.size()];
		return vec.toArray(result);
	}

	String[] listLocallyScopedNames(int typeMask, boolean recursive) {
		ArrayList<String> vec = new ArrayList<String>();
		String[] result;

		listScopedNamesHelper("", typeMask, recursive, new String[0], vec);
		result = new String[vec.size()];
		return vec.toArray(result);
	}

	String[] listLocallyScopedNames(int typeMask, boolean recursive, String[] filterPatterns) {
		ArrayList<String> vec = new ArrayList<String>();
		String[] result;

		listScopedNamesHelper("", typeMask, recursive, filterPatterns, vec);
		result = new String[vec.size()];
		return vec.toArray(result);
	}

	private String[] listLocalNames(int typeMask) {
		ArrayList<String> arrayList;
		Map.Entry<String, ConfigItem> entry;
		Iterator<Entry<String, ConfigItem>> iter;
		ConfigItem item;
		String[] result;

		arrayList = new ArrayList<String>();
		iter = table.entrySet().iterator();
		while (iter.hasNext()) {
			entry = iter.next();
			item = entry.getValue();
			if ((item.getType() & typeMask) != 0) {
				arrayList.add(item.getName());
			}
		}
		result = new String[arrayList.size()];
		arrayList.toArray(result);
		return result;
	}

	private void listScopedNamesHelper(String prefix, int typeMask, boolean recursive, String[] filterPatterns, ArrayList<String> arrayList) {
		Map.Entry<String, ConfigItem> entry;
		Iterator<Entry<String, ConfigItem>> iter;
		ConfigItem item;
		ConfigScope scope;
		boolean isPrefixEmpty;
		String scopedName;

		isPrefixEmpty = prefix.equals("");
		iter = table.entrySet().iterator();
		while (iter.hasNext()) {
			entry = iter.next();
			item = entry.getValue();
			if (isPrefixEmpty) {
				scopedName = item.getName();
			} else {
				scopedName = prefix + "." + item.getName();
			}
			if ((item.getType() & typeMask) != 0 && listFilter(scopedName, filterPatterns)) {
				arrayList.add(scopedName);
			}
			if (recursive && item.getType() == Configuration.CFG_SCOPE) {
				scope = item.getScopeVal();
				scope.listScopedNamesHelper(scopedName, typeMask, true, filterPatterns, arrayList);
			}
		}
	}

	boolean removeItem(String name) {
		ConfigItem item;
		item = table.get(name);
		if (item != null) {
			table.remove(name);
		}
		return item != null;
	}

	ConfigScope rootScope() {
		ConfigScope scope;

		scope = this;
		while (scope.parentScope != null) {
			scope = scope.parentScope;
		}
		return scope;
	}
}
