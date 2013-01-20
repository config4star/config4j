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
import java.util.ArrayList;
import java.util.StringTokenizer;


class Util
{

	public static void assertion(boolean b)
	{
		if (!b) {
			throw new AssertionError("assertion failed");
		}
	}


	public static void assertion(boolean b, String msg)
	{
		if (!b) {
			throw new IllegalStateException(
				"assertion failure: " + msg);
		}
	}


	public static String[] splitScopedNameIntoArray(String str)
	{
		ArrayList			list = null;
		StringTokenizer		st = null;
		String				array[] = null;

		list = new ArrayList();
		st = new StringTokenizer(str, ".");
		while (st.hasMoreTokens()) {
			list.add(st.nextToken());
		}
		array = new String[list.size()];
		return (String[])(list.toArray(array));
	}


	public static boolean isCmdInDir(String cmd, String dir)
	{
		File				file;
		String				fileName;
		String[]			extArray;
		int					i;
		
		if (File.separator.equals("\\")) {
			extArray = new String[]{".exe", ".bat", ""};
		} else {
			extArray = new String[]{""};
		}

		for (i = 0; i < extArray.length; i++) {
			fileName = dir + File.separator + cmd + extArray[i];
			file = new File(fileName);
			if (file.isFile()) {
				return true;
			}
		}
		return false;
	}


	public static boolean execCmd(String cmd, StringBuffer output)
	{
		Process					p;
		StreamReaderThread		outThread;
		StreamReaderThread		errThread;
		int						exitVal;

		//--------
		// Ensure that "output" is empty.
		//--------
		output.delete(0, output.length());

		//--------
		// Execute the command and capture its stdout
		// (and stderr too if the proces has a non-zero exit status).
		//
		// Note: the article "When Runtime.exec() won't" by Michael
		// C. Daconta, JavaWorld.com, 12/29/00, explains that the
		// stdout and stderr of an executed process should be
		// read concurrently by separate threads to avoid the
		// possibility of deadlock. You can find the article at:
		// www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
		//--------
		try {
			p = Runtime.getRuntime().exec(cmd);
			outThread = new StreamReaderThread(p.getInputStream());
			errThread = new StreamReaderThread(p.getErrorStream());
			outThread.start();
			errThread.start();
			exitVal = p.waitFor();
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
			if (exitVal == 0) {
				output.append(outThread.getBuf());
			} else {
				output.append(outThread.getBuf());
				output.append("\n");
				output.append(errThread.getBuf());
			}
		} catch(Exception ex) {
			exitVal = 1;
			output.append("cannot execute '" + cmd + "': "
					+ ex.getMessage());
		}
		return (exitVal == 0);
	}

}
