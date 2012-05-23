//----------------------------------------------------------------------
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


public class Logger
{
	public static final int NO_LOGS_LEVEL = 0;
	public static final int ERROR_LEVEL   = 1;
	public static final int WARN_LEVEL    = 2;
	public static final int INFO_LEVEL    = 3;
	public static final int DEBUG_LEVEL   = 4;

	public static void error(int logLevel, String msg)
	{
		if (logLevel >= ERROR_LEVEL) {
			System.out.println(msg);
		}
	}


	public static void warn(int logLevel, String msg)
	{
		if (logLevel >= WARN_LEVEL) {
			System.out.println(msg);
		}
	}


	public static void info(int logLevel, String msg)
	{
		if (logLevel >= INFO_LEVEL) {
			System.out.println(msg);
		}
	}


	public static void debug(int logLevel, String msg)
	{
		if (logLevel >= DEBUG_LEVEL) {
			System.out.println(msg);
		}
	}

}
