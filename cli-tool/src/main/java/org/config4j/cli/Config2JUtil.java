//-----------------------------------------------------------------------
// Copyright 2011 Ciaran McHale and Łukasz Rżanek.
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

package org.config4j.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import lombok.Data;
import lombok.val;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

@Data
class Config2JUtil {
	private static final String INDENT1 = "    ";
	private static final String INDENT2 = "        ";
	private static final String CR = System.lineSeparator();

	private CLIParameters params;

	public Config2JUtil(String progName) {
		params = new CLIParameters(progName);
	}

	boolean parseCmdLineArgs(String args[]) {
		val jCommander = new JCommander(params);

		try {
			jCommander.parse(args);
		} catch (ParameterException e) {
			System.out.println(e.getLocalizedMessage());
		}

		if (params.isHelp()) {
			jCommander.usage();
			return false;
		} else {
			return true;
		}
	}

	private void wrapDescription(StringBuilder out, int indent, String description) {
		int max = 79;
		String[] words = description.split(" ");
		int current = indent;
		int i = 0;
		while (i < words.length) {
			String word = words[i];
			if (word.length() > max || current + word.length() <= max) {
				out.append(" ").append(word);
				current += word.length() + 1;
			} else {
				out.append("\n").append((indent + 1)).append(word);
				current = indent;
			}
			i++;
		}
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

	boolean generateJavaClass(String[] schema) {
		String dirSeparator = System.getProperty("file.separator");
		String javaFileName = params.getOutputDir() + dirSeparator + params.getClassName() + ".java";
		BufferedReader in;
		FileReader fIn = null;
		PrintWriter out = null;
		BufferedWriter bOut = null;
		FileWriter fOut = null;
		boolean result;

		// --------
		// Open the input (configuration) file.
		// --------
		try {
			fIn = new FileReader(params.getCfgFile());
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

	private void writeClassToFile(BufferedReader in, PrintWriter out, String[] schema) throws IOException {
		int i;
		int schemaLen;
		int ch;
		int count;
		String ctorAccess;
		String accessorStatic;
		String singletonDot;

		if (params.isWantSingleton()) {
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
		if (!params.getPackageName().equals("")) {
			out.print("package " + params.getPackageName() + ";" + CR + CR);
		}
		out.print("//--------------------------------------------------" + "--------------------" + CR
				+ "// WARNING: this file was generated by " + params.getProgName() + "." + CR + "// DO NOT EDIT." + CR
				+ "//--------------------------------------------------" + "--------------------" + CR + CR + CR
				+ (params.isClassPublic() ? "public " : "") + "class " + params.getClassName() + CR + "{" + CR + INDENT1 + ctorAccess
				+ params.getClassName() + "()" + CR
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
		if (params.isWantSingleton()) {
			out.print(INDENT1 + "private static " + params.getClassName() + " singleton = new " + params.getClassName() + "();" + CR);
		}
		out.print("}" + CR);
	}

	private void outputEscapedString(PrintWriter out, String str) throws IOException {
		int i;
		int len;

		len = str.length();
		for (i = 0; i < len; i++) {
			outputEscapedChar(out, str.charAt(i));
		}
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
}
