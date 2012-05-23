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
		this.wantDiagnostics = false;
		this.cfg = Configuration.create();
	}


	public FooConfiguration(boolean wantDiagnostics)
	{
		this.wantDiagnostics = wantDiagnostics;
		this.cfg = Configuration.create();
	}


	public void parse(
			String			cfgInput,
			String			cfgScope,
			String			secInput,
			String			secScope) throws FooConfigurationException
	{
		SchemaValidator		sv = new SchemaValidator();

		try {
			//--------
			// Set non-default security, if supplied
			// Parse config input, if supplied
			// Set fallback configuration
			//--------
			if (!secInput.equals("")) {
				cfg.setSecurityConfiguration(secInput, secScope);
			}
			if (!cfgInput.equals("")) {
				cfg.parse(cfgInput);
			}
			cfg.setFallbackConfiguration(Configuration.INPUT_STRING,
										  FallbackConfiguration.getString());

			//--------
			// Perform schema validation.
			//--------
			sv.setWantDiagnostics(wantDiagnostics);
			sv.parseSchema(FallbackConfiguration.getSchema());
			sv.validate(cfg.getFallbackConfiguration(), "", "",
						SchemaValidator.FORCE_REQUIRED);
			sv.validate(cfg, cfgScope, "");

			//--------
			// Cache configuration variables in instance variables for 
			// faster access.
			//--------
			connectionTimeout = cfg.lookupDurationMilliseconds(cfgScope,
														"connection_timeout");
			rpcTimeout = cfg.lookupDurationMilliseconds(cfgScope,
														"rpc_timeout");
			idleTimeout = cfg.lookupDurationMilliseconds(cfgScope,
														"idle_timeout");
			logFile = cfg.lookupString(cfgScope, "log.file");
			logLevel = cfg.lookupInt(cfgScope, "log.level");
			host = cfg.lookupString(cfgScope, "host");
			port = cfg.lookupInt(cfgScope, "port");
		} catch(ConfigurationException ex) {
			throw new FooConfigurationException(ex.getMessage());
		}
	}

	//--------
	// Accessors for instance variables.
	//--------
	public int    getConnectionTimeout()	{ return connectionTimeout; }
	public int    getRpcTimeout()			{ return rpcTimeout; }
	public int    getIdleTimeout()			{ return idleTimeout; }
	public String getLogFile()				{ return logFile; }
	public int    getLogLevel()				{ return logLevel; }
	public String getHost()					{ return host; }
	public int    getPort()					{ return port; }

	//--------
	// Instance variables
	//--------
	private Configuration	cfg;
	private boolean			wantDiagnostics;
	private int				connectionTimeout;
	private int				rpcTimeout;
	private int				idleTimeout;
	private String			logFile;
	private int				logLevel;
	private String			host;
	private int				port;

}
