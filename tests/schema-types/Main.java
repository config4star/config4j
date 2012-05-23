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


import org.config4j.Configuration;
import org.config4j.ConfigurationException;
import org.config4j.SchemaValidator;


public class Main
{
	private static boolean		wantDiagnostics;
	private static String		cfgFile;

	public static void main(String[] args)
	{
		Configuration			cfg = Configuration.create();
		String					buf = null;
		String					exPattern = null;
		String[]				testSchema;
		String[]				goodScopes = null;
		String[]				badScopes = null;
		SchemaValidator			fileSv = new SchemaValidator();
		SchemaValidator			testSv = new SchemaValidator();
		int						passedCount = 0;
		int						totalCount = 0;
		int						i;
		int						len;
		String[]				fileSchema = new String[] {
										"testSchema = list[string]",
										"good = scope",
										"bad = scope",
										"@ignoreScopesIn good",
										"@ignoreScopesIn bad",
		};

		parseCmdLineArgs(args);

		//--------
		// Parse and validate the configuration file. Then get the testSchema
		// and lists of the names of good and bad sub-scopes.
		//--------
		try {
			cfg.parse(cfgFile);
			fileSv.parseSchema(fileSchema);
			fileSv.validate(cfg, "", "");

			testSchema = cfg.lookupList("testSchema", "");
			goodScopes = cfg.listFullyScopedNames("good", "",
											Configuration.CFG_SCOPE, false);
			badScopes = cfg.listFullyScopedNames("bad", "",
											Configuration.CFG_SCOPE, false);
			testSv.setWantDiagnostics(wantDiagnostics);
			testSv.parseSchema(testSchema);
		} catch(ConfigurationException ex) {
			System.out.println(ex.getMessage());
			System.exit(1);
		}

		//--------
		// Schema validation should succeed for every sub-scope withinin "good".
		//--------
		len = goodScopes.length;
		for (i = 0; i < len; i++) {
			try {
				testSv.validate(cfg, goodScopes[i], "");
				passedCount ++;
			} catch(ConfigurationException ex) {
				try {
					buf = cfg.dump(true, goodScopes[i], "");
				} catch(ConfigurationException ex2) {
					System.out.println("dump() failed: " + ex2.getMessage());
						System.exit(1);
				}
				System.out.println("\n\n--------");
				System.out.println(ex.getMessage() + "\n\n" + buf
									+ "--------\n");
			}
		}

		//--------
		// Schema validation should fail for every sub-scope within "bad".
		//--------
		len = badScopes.length;
		for (i = 0; i < len; i++) {
			try {
				exPattern = cfg.lookupString(badScopes[i], "exception");
				testSv.validate(cfg, badScopes[i], "");
				try {
					buf = cfg.dump(true, badScopes[i], "");
				} catch(ConfigurationException ex2) {
					System.out.println("dump() failed: " + ex2.getMessage());
						System.exit(1);
				}
				System.out.println("\n\n--------");
				System.out.println("Validation succeeded for scope '"
							+ badScopes[i] + "'\n" + buf + "--------\n");
			} catch(ConfigurationException ex) {
				if (Configuration.patternMatch(ex.getMessage(), exPattern)) {
					passedCount ++;
				} else {
					System.out.print("\n\n--------\n");
					System.out.print("Unexpected exception for scope \""
							+ badScopes[i] + "\"\nPattern \"" + exPattern
							+ "\" does not match exception \n\""
							+ ex.getMessage() + "\"\n--------\n\n");
				}
			}
		}

		totalCount = goodScopes.length + badScopes.length;
		System.out.println(passedCount + " tests out of " + totalCount
						   + " passed\n");
		if (passedCount == totalCount) {
			System.exit(0);
		} else {
			System.exit(1);
		}
	}


	private static void
	parseCmdLineArgs(String[] args)
	{
		int						i;

		cfgFile = null;
		wantDiagnostics = false;

		for (i = 0; i < args.length; i++) {
			if (args[i].equals("-h")) {
				usage();
			} else if (args[i].equals("-diagnostics")) {
				wantDiagnostics = true;
			} else if (args[i].equals("-nodiagnostics")) {
				wantDiagnostics = false;
			} else if (args[i].equals("-cfg")) {
				if (i == args.length-1) { usage(); }
				cfgFile = args[i+1];
				i++;
			} else {
				System.err.println("\nUnrecognised option '" + args[i] + "'");
				usage();
			}
		}
		if (cfgFile == null) {
			System.err.println("\nYou must specify '-cfg <file.cfg>'");
			usage();
		}
	}



	private static void
	usage()
	{
		System.err.println("\n"
			+ "usage: schema-testsuite <options>\n"
			+ "\n"
			+ "The <options> can be:\n"
			+ "  -h               Print this usage statement\n"
			+ "  -cfg <file.cfg>  Parse specified configuration file\n"
			+ "  -diagnostics     Prints diagnostics from schema validator\n"
			+ "  -nodiagnostics   No diagnostics (default)\n"
			+ "\n");
		System.exit(1);
	}

}

