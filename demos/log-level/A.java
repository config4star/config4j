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

public class A
{
	public A(FooConfiguration cfg)
	{
		logLevelOp1 = cfg.getLogLevel("A.op1");
		logLevelOp2 = cfg.getLogLevel("A.op2");
		logLevelOp3 = cfg.getLogLevel("A.op3");
	}


	public void op1()
	{
		Logger.error(logLevelOp1, "A.op1(): this is an error message");
		Logger.info(logLevelOp1,  "A.op1(): this is an information message");
		Logger.warn(logLevelOp1,  "A.op1(): this is a warning message");
		Logger.debug(logLevelOp1, "A.op1(): this is a debug message");
	}


	public void op2()
	{
		Logger.error(logLevelOp2, "A.op2(): this is an error message");
		Logger.info(logLevelOp2,  "A.op2(): this is an information message");
		Logger.warn(logLevelOp2,  "A.op2(): this is a warning message");
		Logger.debug(logLevelOp2, "A.op2(): this is a debug message");
	}


	public void op3()
	{
		Logger.error(logLevelOp3, "A.op3(): this is an error message");
		Logger.info(logLevelOp3,  "A.op3(): this is an information message");
		Logger.warn(logLevelOp3,  "A.op3(): this is a warning message");
		Logger.debug(logLevelOp3, "A.op3(): this is a debug message");
	}

	//--------
	// Instance variables
	//--------
	private int		logLevelOp1;
	private int		logLevelOp2;
	private int		logLevelOp3;

}
