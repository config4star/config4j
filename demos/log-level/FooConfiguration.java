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


public class FooConfiguration
{
	public FooConfiguration()
	{
		this.cfg = Configuration.create();
		logLevels = null;
	}


	public void parse(String cfgInput) throws FooConfigurationException
	{
		parse(cfgInput, "", "", "");
	}


	public void parse(
		String				cfgInput,
		String				cfgScope) throws FooConfigurationException
	{
		parse(cfgInput, cfgScope, "", "");
	}


	public void parse(
		String				cfgInput,
		String				cfgScope,
		String				secInput) throws FooConfigurationException
	{
		parse(cfgInput, cfgScope, secInput, "");
	}


	public void parse(
			String	cfgInput,
			String	cfgScope,
			String	secInput,
			String	secScope) throws FooConfigurationException
	{
		SchemaValidator		sv = new SchemaValidator();
		String[]			schema = new String[] {
			"@typedef logLevel = int[0, 4]",
			"log_levels = table[string,operation-name, logLevel,log-level]",
		};

		try {
			//--------
			// Set non-default security, if supplied. Then parse the
			// configuration input. Finally, perform schema validation.
			//--------
			if (!secInput.equals("")) {
				cfg.setSecurityConfiguration(secInput, secScope);
			}
			cfg.parse(cfgInput);

			//--------
			// Perform schema validation.
			//--------
			sv.parseSchema(schema);
			sv.validate(cfg, cfgScope, "");

			//--------
			// Cache configuration variables in instance variables for 
			// faster access.
			//--------
			logLevels = cfg.lookupList(cfgScope, "log_levels");
		} catch(ConfigurationException ex) {
			throw new FooConfigurationException(ex.getMessage());
		}
	}


	public int getLogLevel(String opName)
	{
		int				i;
		String			pattern;
		String			logLevelStr;

		for (i = 0; i < logLevels.length; i += 2) {
			pattern     = logLevels[i + 0];
			logLevelStr = logLevels[i + 1];
			if (Configuration.patternMatch(opName, pattern)) {
				return Integer.parseInt(logLevelStr);
			}
		}
		return 1; // default log level
	}


	//--------
	// Instance variables
	//--------
	private Configuration	cfg;
	private	String[]		logLevels;

}
