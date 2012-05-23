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

package testclient;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.config4j.Configuration;


public class Util
{

	public static boolean isStringInListOfPatterns(
		String			str,
		String[]		patterns)
	{
		int				i;

		for (i = 0; i < patterns.length; i++) {
			if (Configuration.patternMatch(str, patterns[i])) {
				return true;
			}
		}
		return false;
	}


	public static String replace(
		String			origStr,
		String			searchStr,
		String			replacementStr)
	{
		StringBuffer	result;
		int				origStrLen;
		int				searchStrLen;
		int				currStart;
		int				pIndex;

		result = new StringBuffer();
		origStrLen = origStr.length();
		searchStrLen = searchStr.length();
		currStart = 0;
		pIndex = origStr.indexOf(searchStr, currStart);
		while (pIndex != -1) {
			result.append(origStr.substring(currStart, pIndex));
			result.append(replacementStr);
			currStart = pIndex + searchStrLen;
			pIndex = origStr.indexOf(searchStr, currStart);
		}
		result.append(origStr.substring(currStart));
		return result.toString();
	}


	public static String replace(
		String				origStr,
		Map					searchAndReplaceMap)
	{
		Iterator			iter;
		Map.Entry			entry;
		Set					entrySet;
		int					i;
		String[]			pairs;

		entrySet = searchAndReplaceMap.entrySet();
		pairs = new String[entrySet.size() * 2];
		iter = entrySet.iterator();
		for (i = 0; i < pairs.length; i += 2) {
			entry = (Map.Entry)iter.next();
			pairs[i + 0] = (String)entry.getKey();
			pairs[i + 1] = (String)entry.getValue();
		}
		return replace(origStr, pairs);
	}


	public static String replace(
		String						origStr,
		String[]					searchAndReplacePairs)
	{
		int							i;
		int							currIndex;
		String						subStr;
		String						replaceStr;
		StringBuffer				result;
		SearchAndReplacePair[]		pairs;
		SearchAndReplacePair		nextPair;

		pairs = new SearchAndReplacePair[searchAndReplacePairs.length / 2];
		for (i = 0; i < searchAndReplacePairs.length; i += 2) {
			pairs[i/2] = new SearchAndReplacePair(origStr,
												  searchAndReplacePairs[i + 0],
												  searchAndReplacePairs[i + 1]);
		}

		result = new StringBuffer();
		currIndex = 0;
		nextPair = findNextPair(origStr, currIndex, pairs);
		while (nextPair != null) {
			//--------
			// Debug diagnostics
			//--------
			//System.out.println("replace()"
			//	+ ": currIndex=" + currIndex
			//	+ "; indexOf=" + nextPair.indexOf
			//	+ "; found='" + nextPair.search + "'");
			subStr = origStr.substring(currIndex, nextPair.indexOf);
			result.append(subStr);
			result.append(nextPair.replace);
			currIndex = nextPair.indexOf + nextPair.length;
			for (i = 0; i < pairs.length; i++) {
				pairs[i].findNext(currIndex);
			}
			nextPair = findNextPair(origStr, currIndex, pairs);
		}
		subStr = origStr.substring(currIndex);
		result.append(subStr);

		return result.toString();
	}


	private static SearchAndReplacePair findNextPair(
		String						origStr,
		int							currIndex,
		SearchAndReplacePair[]		pairs)
	{
		int							i;
		SearchAndReplacePair		bestSoFar;
		SearchAndReplacePair		item;

		bestSoFar = null;
		for (i = 0; i < pairs.length; i++) {
			item = pairs[i];
			if (item.indexOf == -1) {
				continue;
			}
			if (bestSoFar == null) {
				bestSoFar = item;
				continue;
			}
			if (bestSoFar.indexOf < item.indexOf) {
				continue;
			}
			if (bestSoFar.indexOf > item.indexOf) {
				bestSoFar = item;
				continue;
			}
			if (bestSoFar.length < item.length) {
				bestSoFar = item;
			}
		}
		return bestSoFar;
	}

}
