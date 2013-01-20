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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

class Config2JUtil {
	private static final String CR = System.getProperty("line.separator");
	private static final String INDENT1 = "    ";
	private static final String INDENT2 = "        ";

	private String cfgFileName;

	private String className;

	private boolean isClassPublic;

	private String outputDir;

	private String packageName;

	// --------
	// Instance variables
	// --------
	private String progName;

	private String schemaOverrideCfg;

	private String schemaOverrideScope;

	private boolean wantSchema;

	private boolean wantSingleton;

	Config2JUtil(String progName) {
		this.progName = progName;
		cfgFileName = null;
		schemaOverrideCfg = null;
		schemaOverrideScope = "";
		className = null;
		isClassPublic = false;
		outputDir = ".";
		wantSingleton = false;
		wantSchema = true;
		packageName = "";
	}

	boolean generateJavaClass(String[] schema) {
		String javaFileName;
		String dirSeparator;
		BufferedReader in;
		FileReader fIn;
		PrintWriter out;
		BufferedWriter bOut;
		FileWriter fOut;
		boolean result;

		dirSeparator = System.getProperty("file.separator");
		javaFileName = outputDir + dirSeparator + className + ".java";
		fIn = null;
		fOut = null;

		// --------
		// Open the input (configuration) file.
		// --------
		try {
			fIn = new FileReader(cfgFileName);
			in = new BufferedReader(fIn);
		} catch (IOException ex) {
			System.err.println(ex.getMessage());
			if (fIn != null) {
				try {
					fIn.close();
				} catch (IOException ex2) {
				}
			}
			return false;
		}

		// --------
		// Open the output (Java) file.
		// --------
		try {
			fOut = new FileWriter(javaFileName);
			bOut = new BufferedWriter(fOut);
			out = new PrintWriter(bOut);
		} catch (IOException ex) {
			System.err.println(ex.getMessage());
			if (fOut != null) {
				try {
					fOut.close();
				} catch (IOException ex2) {
				}
			}
			try {
				in.close();
			} catch (IOException ex3) {
			}
			return false;
		}

		result = true;
		try {
			writeClassToFile(in, out, schema);
		} catch (IOException ex) {
			result = false;
		}

		// --------
		// Close file descriptor.
		// --------
		try {
			in.close();
			out.close();
		} catch (IOException ex2) {
			result = false;
		}

		return result;
	}

	String getCfgFileName() {
		return cfgFileName;
	}

	String getSchemaOverrideCfg() {
		return schemaOverrideCfg;
	}

	String getSchemaOverrideScope() {
		return schemaOverrideScope;
	}

	private boolean isValidPackageName(String str) {
		StringTokenizer st;
		int i;
		int len;
		String name;

		st = new StringTokenizer(str, ".");
		while (st.hasMoreTokens()) {
			name = st.nextToken();
			if (name.equals("")) {
				return false;
			}
			if (!Character.isJavaIdentifierStart(name.charAt(0))) {
				return false;
			}
			len = name.length();
			for (i = 1; i < len; i++) {
				if (!Character.isJavaIdentifierPart(name.charAt(i))) {
					return false;
				}

			}
		}
		return true;
	}

	void outputEscapedChar(PrintWriter out, char ch) {
		switch (ch) {
			case '\\':
				out.print("\\\\");
				break;
			case '\t':
				out.print("\\t");
				break;
			case '\n':
				// out.print("\\n");
				out.print("\" + CR + \"");
				break;
			case '"':
				out.print("\\\"");
				break;
			default:
				out.print(ch);
		}
	}

	private void outputEscapedString(PrintWriter out, String str) throws IOException {
		int i;
		int len;

		len = str.length();
		for (i = 0; i < len; i++) {
			outputEscapedChar(out, str.charAt(i));
		}
	}

	boolean parseCmdLineArgs(String args[]) {
		int i;

		for (i = 0; i < args.length; i++) {
			if (args[i].equals("-cfg")) {
				if (i == args.length - 1) {
					usage("");
					return false;
				}
				cfgFileName = args[i + 1];
				i++;
			} else if (args[i].equals("-noschema")) {
				wantSchema = false;
			} else if (args[i].equals("-class")) {
				if (i == args.length - 1) {
					usage("");
					return false;
				}
				className = args[i + 1];
				i++;
			} else if (args[i].equals("-outDir")) {
				if (i == args.length - 1) {
					usage("");
					return false;
				}
				outputDir = args[i + 1];
				i++;
			} else if (args[i].equals("-public")) {
				isClassPublic = true;
			} else if (args[i].equals("-schemaOverrideCfg")) {
				if (i == args.length - 1) {
					usage("");
					return false;
				}
				schemaOverrideCfg = args[i + 1];
				i++;
			} else if (args[i].equals("-schemaOverrideScope")) {
				if (i == args.length - 1) {
					usage("");
					return false;
				}
				schemaOverrideScope = args[i + 1];
				i++;
			} else if (args[i].equals("-package")) {
				if (i == args.length - 1) {
					usage("");
					return false;
				}
				if (!isValidPackageName(args[i + 1])) {
					System.err.print(CR + "Invalid package '" + args[i + 1] + "'");
					usage("");
					return false;
				}
				packageName = args[i + 1];
				i++;
			} else if (args[i].equals("-singleton")) {
				wantSingleton = true;
			} else {
				usage(args[i]);
				return false;
			}
		}
		if (className == null || cfgFileName == null) {
			usage("");
			return false;
		}
		return true;
	}

	private void usage(String unknownArg) {
		System.err.print(CR);
		if (!unknownArg.equals("")) {
			System.err.print("unknown argument '" + unknownArg + "'" + CR);
		}
		System.err.print("usage: java " + progName + " -cfg <file.cfg> -class <class>" + CR + "options are:" + CR
		        + "\t-outDir <directory>  Generate the class in the " + "specified directory" + CR
		        + "\t-public              Make the generated class public" + CR + "\t-noschema            Do not generate a schema" + CR
		        + "\t-schemaOverrideCfg   <file.cfg>   " + CR + "\t-schemaOverrideScope <scope> " + CR
		        + "\t-package x.y.z       Generate class into " + "specified package" + CR
		        + "\t-singleton           generate a singleton class" + CR);
	}

	boolean wantSchema() {
		return wantSchema;
	}

	private void writeClassToFile(BufferedReader in, PrintWriter out, String[] schema) throws IOException {
		int i;
		int schemaLen;
		int ch;
		int count;
		String ctorAccess;
		String accessorStatic;
		String singletonDot;

		if (wantSingleton) {
			ctorAccess = "private ";
			accessorStatic = "static ";
			singletonDot = "singleton.";
		} else {
			ctorAccess = "public ";
			accessorStatic = " ";
			singletonDot = "";
		}
		if (schema == null) {
			schemaLen = 0;
		} else {
			schemaLen = schema.length;
		}
		if (!packageName.equals("")) {
			out.print("package " + packageName + ";" + CR + CR);
		}
		out.print("//--------------------------------------------------" + "--------------------" + CR
		        + "// WARNING: this file was generated by " + progName + "." + CR + "// DO NOT EDIT." + CR
		        + "//--------------------------------------------------" + "--------------------" + CR + CR + CR
		        + (isClassPublic ? "public " : "") + "class " + className + CR + "{" + CR + INDENT1 + ctorAccess + className + "()" + CR
		        + INDENT1 + "{" + CR);
		if (schema != null) {
			out.print(INDENT2 + "schema = new String[" + schemaLen + "];" + CR);
		}
		for (i = 0; i < schemaLen; i++) {
			out.print(INDENT2 + "schema[" + i + "] = \"");
			outputEscapedString(out, schema[i]);
			out.print("\";" + CR);
		}
		if (schema != null) {
			out.print("" + CR);
		}
		out.print(INDENT2 + "str = new StringBuffer();" + CR);
		out.print(INDENT2 + "str.append(\"");
		ch = in.read();
		count = 0;
		while (ch != -1) {
			if (ch == '\r') {
				ch = in.read();
				continue;
			}
			count++;
			if (ch == '\n') {
				out.print("\" + CR");
			} else {
				outputEscapedChar(out, (char) ch);
			}
			if (ch == '\n' || count == 48) {
				if (ch == '\n') {
					out.print(");" + CR);
				} else {
					out.print("\");" + CR);
				}
				out.print(INDENT2 + "str.append(\"");
				count = 0;
			}
			ch = in.read();
		}
		out.print("\");" + CR);
		out.print(INDENT1 + "}" + CR);
		out.print(CR);
		if (schema != null) {
			out.print(INDENT1 + "public " + accessorStatic + "String[] getSchema() " + "{ return " + singletonDot + "schema; }" + CR);
		}
		out.print(INDENT1 + "public " + accessorStatic + "String   getString() " + "{ return " + singletonDot + "str.toString(); }" + CR);
		out.print(CR);
		if (schema != null) {
			out.print(INDENT1 + "private String[] schema;" + CR);
		}
		out.print(INDENT1 + "private StringBuffer str;" + CR);
		out.print(INDENT1 + "private static final String CR = System.getProperty(\"line.separator\");" + CR);
		if (wantSingleton) {
			out.print(INDENT1 + "private static " + className + " singleton = new " + className + "();" + CR);
		}
		out.print("}" + CR);
	}
}
