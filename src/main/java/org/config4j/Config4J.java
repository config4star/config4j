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

class Config4J {

	private static String cfgSource;
	private static String cmd;
	private static String[] filterPatterns;
	private static int forceMode;
	private static boolean isRecursive;
	private static String name;
	private static String schemaName;
	private static String schemaSource;
	private static String scope;
	private static String secScope;
	private static String secSource;
	private static int types;
	private static boolean wantDiagnostics;
	private static boolean wantExpandedUidNames;

	public static void main(String[] args) {
		String str;
		Configuration cfg;
		Configuration secCfg;
		Configuration schemaCfg;
		Configuration secDumpCfg;
		SchemaValidator sv = new SchemaValidator();
		String secDumpScope;
		String[] vec;
		String[] schema;
		int i;
		String[] names;
		String fullyScopedName;

		cfg = Configuration.create();
		secCfg = Configuration.create();
		schemaCfg = Configuration.create();

		parseCmdLineArgs(args, cfg);
		try {
			if (secSource != null) {
				secCfg.parse(secSource);
				cfg.setSecurityConfiguration(secCfg, secScope);
			}
			cfg.parse(cfgSource);
		} catch (ConfigurationException ex) {
			System.err.println(ex.getMessage());
			System.exit(1);
		}
		fullyScopedName = Configuration.mergeNames(scope, name);

		if (cmd.equals("parse")) {
			// --------
			// Nothing else to do
			// --------
		} else if (cmd.equals("validate")) {
			try {
				schemaCfg.parse(schemaSource);
				schema = schemaCfg.lookupList(schemaName, "");
				sv.setWantDiagnostics(wantDiagnostics);
				sv.parseSchema(schema);
				sv.validate(cfg, scope, name, isRecursive, types, forceMode);
			} catch (ConfigurationException ex) {
				System.err.println(ex.getMessage());
			}
		} else if (cmd.equals("slist")) {
			try {
				names = cfg.listFullyScopedNames(scope, name, types, isRecursive, filterPatterns);
				for (i = 0; i < names.length; i++) {
					System.out.println(names[i]);
				}
			} catch (ConfigurationException ex) {
				System.err.println(ex.getMessage());
			}
		} else if (cmd.equals("llist")) {
			try {
				names = cfg.listLocallyScopedNames(scope, name, types, isRecursive, filterPatterns);
				for (i = 0; i < names.length; i++) {
					System.out.println(names[i]);
				}
			} catch (ConfigurationException ex) {
				System.err.println(ex.getMessage());
			}
		} else if (cmd.equals("type")) {
			switch (cfg.type(scope, name)) {
				case Configuration.CFG_STRING:
					System.out.println("string");
					break;
				case Configuration.CFG_LIST:
					System.out.println("list");
					break;
				case Configuration.CFG_SCOPE:
					System.out.println("scope");
					break;
				case Configuration.CFG_NO_VALUE:
					System.out.println("no_value");
					break;
				default:
					Util.assertion(false); // Bug!
					break;
			}
		} else if (cmd.equals("print")) {
			try {
				switch (cfg.type(scope, name)) {
					case Configuration.CFG_STRING:
						str = cfg.lookupString(scope, name);
						System.out.println(str);
						break;
					case Configuration.CFG_LIST:
						vec = cfg.lookupList(scope, name);
						for (i = 0; i < vec.length; i++) {
							System.out.println(vec[i]);
						}
						break;
					case Configuration.CFG_SCOPE:
						System.err.println("'" + fullyScopedName + "' is a scope");
						break;
					case Configuration.CFG_NO_VALUE:
						System.err.println("'" + fullyScopedName + "' does not exist");
						break;
					default:
						Util.assertion(false); // Bug!
						break;
				}
			} catch (ConfigurationException ex) {
				System.err.println(ex.getMessage());
			}
		} else if (cmd.equals("dumpSec")) {
			try {
				secDumpCfg = cfg.getSecurityConfiguration();
				secDumpScope = cfg.getSecurityConfigurationScope();
				str = secDumpCfg.dump(wantExpandedUidNames, secDumpScope, "allow_patterns");
				System.out.println(str);
				str = secDumpCfg.dump(wantExpandedUidNames, secDumpScope, "deny_patterns");
				System.out.println(str);
				str = secDumpCfg.dump(wantExpandedUidNames, secDumpScope, "trusted_directories");
				System.out.println(str);
			} catch (ConfigurationException ex) {
				System.err.println(ex.getMessage());
			}
		} else if (cmd.equals("dump")) {
			try {
				str = cfg.dump(wantExpandedUidNames, scope, name);
				System.out.print(str);
			} catch (ConfigurationException ex) {
				System.err.println(ex.getMessage());
			}
		} else {
			Util.assertion(false); // Bug!
		}
	}

	private static void parseCmdLineArgs(String[] args, Configuration cfg) {
		int i;
		ArrayList<String> patterns;

		cmd = null;
		wantExpandedUidNames = true;
		scope = "";
		name = "";
		cfgSource = null;
		secSource = null;
		secScope = "";
		schemaSource = null;
		schemaName = "";
		wantDiagnostics = false;
		isRecursive = true;
		patterns = new ArrayList<String>();
		types = Configuration.CFG_SCOPE_AND_VARS;

		for (i = 0; i < args.length; i++) {
			if (args[i].equals("-h")) {
				usage("");
			} else if (args[i].equals("-set")) {
				if (i >= args.length - 2) {
					usage("");
				}
				cfg.insertString("", args[i + 1], args[i + 2]);
				i += 2;
			} else if (args[i].equals("-cfg")) {
				if (i == args.length - 1) {
					usage("");
				}
				cfgSource = args[i + 1];
				i++;
			} else if (args[i].equals("-secCfg")) {
				if (i == args.length - 1) {
					usage("");
				}
				secSource = args[i + 1];
				i++;
			} else if (args[i].equals("-secScope")) {
				if (i == args.length - 1) {
					usage("");
				}
				secScope = args[i + 1];
				i++;
			} else if (args[i].equals("-schemaCfg")) {
				if (i == args.length - 1) {
					usage("");
				}
				schemaSource = args[i + 1];
				i++;
			} else if (args[i].equals("-schema")) {
				if (i == args.length - 1) {
					usage("");
				}
				schemaName = args[i + 1];
				i++;
			} else if (args[i].equals("-diagnostics")) {
				wantDiagnostics = true;
			} else if (args[i].equals("-force_optional")) {
				forceMode = SchemaValidator.FORCE_OPTIONAL;
			} else if (args[i].equals("-force_required")) {
				forceMode = SchemaValidator.FORCE_REQUIRED;
			} else if (args[i].equals("-nodiagnostics")) {
				wantDiagnostics = false;
			} else if (args[i].equals("-types")) {
				if (i == args.length - 1) {
					usage("");
				}
				types = stringToTypes(args[i + 1]);
				i++;
				// --------
				// Commands
				// --------
			} else if (args[i].equals("parse")) {
				cmd = args[i];
			} else if (args[i].equals("slist")) {
				cmd = args[i];
			} else if (args[i].equals("llist")) {
				cmd = args[i];
			} else if (args[i].equals("dump")) {
				cmd = args[i];
			} else if (args[i].equals("dumpSec")) {
				cmd = args[i];
			} else if (args[i].equals("type")) {
				cmd = args[i];
			} else if (args[i].equals("print")) {
				cmd = args[i];
			} else if (args[i].equals("validate")) {
				cmd = args[i];
				// --------
				// Arguments to commands
				// --------
			} else if (args[i].equals("-scope")) {
				if (i == args.length - 1) {
					usage("");
				}
				scope = args[i + 1];
				i++;
			} else if (args[i].equals("-filter")) {
				if (i == args.length - 1) {
					usage("");
				}
				patterns.add(args[i + 1]);
				i++;
			} else if (args[i].equals("-name")) {
				if (i == args.length - 1) {
					usage("");
				}
				name = args[i + 1];
				i++;
			} else if (args[i].equals("-recursive")) {
				isRecursive = true;
			} else if (args[i].equals("-norecursive")) {
				isRecursive = false;
			} else if (args[i].equals("-expandUid")) {
				wantExpandedUidNames = true;
			} else if (args[i].equals("-unexpandUid")) {
				wantExpandedUidNames = false;
			} else {
				usage(args[i]);
			}
		}
		if (cfgSource == null) {
			System.err.println("\nYou must specify -cfg <source>\n");
			usage("");
		}
		if (cmd == null) {
			System.err.println("\nYou must specify a command\n\n");
			usage("");
		}
		if (cmd.equals("validate")) {
			if (schemaSource == null) {
				System.err.println("\nThe validate command requires " + "-schemaCfg <source>\n\n");
				usage("");
			}
			if (schemaName.equals("")) {
				System.err.println("\nThe validate command requires " + "-schema <full.name>\n\n");
				usage("");
			}
		}

		filterPatterns = patterns.toArray(new String[patterns.size()]);
	}

	static int stringToTypes(String str) {
		if (str.equals("string")) {
			return Configuration.CFG_STRING;
		} else if (str.equals("list")) {
			return Configuration.CFG_LIST;
		} else if (str.equals("scope")) {
			return Configuration.CFG_SCOPE;
		} else if (str.equals("variables")) {
			return Configuration.CFG_VARIABLES;
		} else if (str.equals("scope_and_vars")) {
			return Configuration.CFG_SCOPE_AND_VARS;
		}
		usage("Invalid value for '-types <...>'");
		return Configuration.CFG_STRING; // Not reached; keep compiler happy
	}

	private static void usage(String optMsg) {
		StringBuffer msg;

		msg = new StringBuffer();
		if (!optMsg.equals("")) {
			msg.append(optMsg + "\n\n");
		}

		msg.append("usage: java org.config4j.Config4J -cfg <source> " + "<command> <options>\n" + "\n"
		        + "<command> can be one of the following:\n" + "  parse               Parse and report errors, if any\n"
		        + "  validate            Validate <scope>.<name>\n" + "  dump                Dump <scope>.<name>\n"
		        + "  dumpSec             Dump the security policy\n" + "  print               Print value of the <scope>.<name> "
		        + "variable\n" + "  type                Print type of the <scope>.<name> entry\n"
		        + "  slist               List scoped names in <scope>.<name>\n"
		        + "  llist               List local names in <scope>.<name>\n" + "\n" + "<options> can be:\n"
		        + "  -h                  Print this usage statement\n" + "  -set <name> <value> Preset name=value in configuration "
		        + "object\n" + "  -scope <scope>      Specify <scope> argument for commands\n"
		        + "  -name <name>        Specify <name> argument for commands\n" + "\n"
		        + "  -secCfg <source>    Override default security policy\n" + "  -secScope           Scope for security policy\n" + "\n"
		        + "  -schemaCfg <source> Source that contains a schema\n"
		        + "  -schema <full.name> Name of schema in '-schemaCfg <source>'\n"
		        + "  -force_optional     Validate with forceMode = FORCE_OPTIONAL\n"
		        + "  -force_required     Validate with forceMode = FORCE_REQUIRED\n"
		        + "  -diagnostics        Print diagnostics during schema " + "validation\n"
		        + "  -nodiagnostics       Do not print diagnostics during schema " + "validation (default)\n" + "\n"
		        + "  -recursive          For llist, slist and validate (default)\n"
		        + "  -norecursive        For llist, slist and validate\n" + "  -filter <pattern>   A filter pattern for sslist and llist\n"
		        + "  -types <types>      For llist, slist and validate\n" + "\n" + "  -expandUid          For dump (default)\n"
		        + "  -unexpandUid        For dump\n" + "\n" + "<types> can be one of the following\n"
		        + "  string, list, scope, variables, scope_and_vars (default)\n" + "\n" + "<source> can be one of the following:\n"
		        + "  file.cfg       A configuration file\n" + "  file#file.cfg  A configuration file\n"
		        + "  exec#<command> Output from executing the specified " + "command\n");
		System.out.print(msg.toString());
		System.exit(1);
	}

}
