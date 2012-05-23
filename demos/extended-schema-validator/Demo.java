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
	private static String	cfgScope;         // set by parseCommandLineArgs()
	private static String	secInput;         // set by parseCommandLineArgs()
	private static String	secScope;         // set by parseCommandLineArgs()
	private static boolean	wantDiagnostics;  // set by parseCommandLineArgs()


	public static void main(String[] args)
	{
		FooConfiguration		cfg;
		String					sep;
		int[]					hexList;
		int						i;

		parseCommandLineArgs(args);
		try {
			//--------
			// Initialize the configuration object.
			//--------
			cfg = new FooConfiguration(wantDiagnostics);
			cfg.parse(cfgInput, cfgScope, secInput, secScope);

			//--------
			// Query the configuration object.
			//--------
			System.out.println("host = " + cfg.getHost());
			System.out.println("timeout = " + cfg.getTimeout());
			System.out.println("hex_byte = " + cfg.getHexByte());
			System.out.println("hex_word = " + cfg.getHexWord());
			hexList = cfg.getHexList();
			System.out.print("hex_list = [");
			sep = "";
			for (i = 0; i < hexList.length; i++) {
				System.out.print(sep + hexList[i]);
				sep = ", ";
			}
			System.out.println("]");
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
		cfgScope = "";
		secInput = "";
		secScope = "";
		wantDiagnostics = false;

		for (i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equals("-h")) {
				usage();
			} else if (arg.equals("-diagnostics")) {
				wantDiagnostics = true;
			} else if (arg.equals("-nodiagnostics")) {
				wantDiagnostics = false;
			} else if (arg.equals("-cfg")) {
				if (i == args.length - 1) { usage(); }
				cfgInput = args[i+1];
				i++;
			} else if (arg.equals("-scope")) {
				if (i == args.length - 1) { usage(); }
				cfgScope = args[i+1];
				i++;
			} else if (arg.equals("-sec")) {
				if (i == args.length - 1) { usage(); }
				secInput = args[i+1];
				i++;
			} else if (arg.equals("-secScope")) {
				if (i == args.length - 1) { usage(); }
				secScope = args[i+1];
				i++;
			} else {
				System.err.println("Unrecognised option '" + arg + "'\n");
				usage();
			}
		}
		if (cfgInput.equals("")) {
			System.err.println("\nYou must specify '-cfg <source>'");
			usage();
		}
	}


	private static void usage()
	{
		String	className = Demo.class.getName();
		System.err.println("\n"
			+ "usage: java " + className + " <options> -cfg <source>\n"
			+ "\n"
			+ "The <options> can be:\n"
			+ "  -h             Print this usage statement\n"
			+ "  -scope         Configuration scope\n"
			+ "  -sec <source>  Override default security configuration\n"
			+ "  -secScope      Security configuration scope\n"
			+ "  -diagnostics   Prints diagnostics from schema validator\n"
			+ "  -nodiagnostics No diagnostics (default)\n"
			+ "\n"
			+ "A configuration <source> can be one of the following:\n"
			+ "  file.cfg       A configuration file\n"
			+ "  file#file.cfg  A configuration file\n"
			+ "  exec#<command> Output from executing the specified command\n"
			+ "\n");
		System.exit(1);
	}

}
