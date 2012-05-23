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


public class FooConfiguration
{

	public FooConfiguration(boolean wantDiagnostics)
	{
		this.wantDiagnostics = wantDiagnostics;
		this.cfg = Configuration.create();
	}


	public void parse(
		String						cfgInput,
		String						scope,
		String						secInput,
		String						secScope) throws FooConfigurationException
	{
		ExtendedSchemaValidator		sv = new ExtendedSchemaValidator();
		int							i;
		String						localName;
		String[]					strList;
		String[]					schema = new String[] {
										"host     = string",
										"timeout  = durationMilliseconds",
										"hex_byte = hex[2]",
										"hex_word = hex[4]",
										"hex_list = list[hex]"
									};

		try {
			//--------
			// Set non-default security, if supplied.
			// Parse config input, if supplied.
			//--------
			if (!secInput.equals("")) {
				cfg.setSecurityConfiguration(secInput, secScope);
			}
			if (!cfgInput.equals("")) {
				cfg.parse(cfgInput);
			}

			//--------
			// Perform schema validation.
			//--------
			sv.setWantDiagnostics(wantDiagnostics);
			sv.parseSchema(schema);
			sv.validate(cfg, scope, "");

			//--------
			// Cache configuration variables in instance variables for 
			// faster access. We use static utility operations on the
			// SchemaTypeHex class to perform lookupHex() and to convert
			// list[hex] to int[].
			//--------
			host = cfg.lookupString(scope, "host");
			timeout = cfg.lookupDurationMilliseconds(scope, "timeout");
			hexByte = SchemaTypeHex.lookupHex(cfg, scope, "hex_byte");
			hexWord = SchemaTypeHex.lookupHex(cfg, scope, "hex_word");
			strList = cfg.lookupList(scope, "hex_list");
			hexList = new int[strList.length];
			for (i = 0; i < strList.length; i++) {
				localName = "hex_list[" + (i+1) + "]";
				hexList[i] = SchemaTypeHex.stringToHex(cfg, scope, localName,
													   strList[i], "hex");
			}
		} catch(ConfigurationException ex) {
			throw new FooConfigurationException(ex.getMessage());
		}
	}


	//--------
	// Acccessors for configuration variables.
	//--------
	public String	getHost()					{ return host; }
	public int		getTimeout()				{ return timeout; }
	public int		getHexByte()				{ return hexByte;}
	public int		getHexWord()				{ return hexWord;}
	public int[]	getHexList()				{ return hexList; }

	//--------
	// Instance variables
	//--------
	private Configuration	cfg;
	private boolean			wantDiagnostics;

	//--------
	// Instance variables to cache configuration variables.
	//--------
	private String			host;
	private int				timeout;
	private int				hexByte;
	private int				hexWord;
	private int[]			hexList;

}
