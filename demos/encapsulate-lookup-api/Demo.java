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


public class Demo
{
	private static String	cfgInput;         // set by parseCommandLineArgs()
	private static String	scope;            // set by parseCommandLineArgs()


	public static void main(String[] args)
	{
		FooConfiguration		cfg = new FooConfiguration();

		parseCommandLineArgs(args);
		try {
			//--------
			// Parse the configuration file.
			//--------
			cfg.parse(cfgInput, scope);

			//--------
			// Query the configuration object.
			//--------
			System.out.println(
				  "host = \"" + cfg.lookupString("host") + "\"\n"
				+ "port = " + cfg.lookupInt("port") + "\n"
				+ "timeout = " + cfg.lookupDurationMilliseconds("timeout")
				+ " (\"" +  cfg.lookupString("timeout") + "\")\n");
		} catch(FooConfigurationException ex) {
			System.err.println(ex.getMessage());
			System.exit(1);
		}
	}


	private static void parseCommandLineArgs(String[] args)
	{
		int					i;
		String				arg;

		cfgInput = "";
		scope = "";

		for (i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equals("-h")) {
				usage();
			} else if (arg.equals("-cfg")) {
				if (i == args.length - 1) { usage(); }
				cfgInput = args[i+1];
				i++;
			} else if (arg.equals("-scope")) {
				if (i == args.length - 1) { usage(); }
				scope = args[i+1];
				i++;
			} else {
				System.err.println("Unrecognised option '" + arg + "'\n");
				usage();
			}
		}
	}


	private static void usage()
	{
		String	className = Demo.class.getName();
		System.err.println("\n"
			+ "usage: java " + className + " <options>\n"
			+ "\n"
			+ "The <options> can be:\n"
			+ "  -h             Print this usage statement\n"
			+ "  -cfg <source>  Parse specified configuration source\n"
			+ "  -scope         Configuration scope\n"
			+ "\n"
			+ "A configuration <source> can be one of the following:\n"
			+ "  file.cfg       A configuration file\n"
			+ "  file#file.cfg  A configuration file\n"
			+ "  exec#<command> Output from executing the specified command\n"
			+ "\n");
		System.exit(1);
	}

}
