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
		this.scope = null;
	}


	public void parse(String cfgSource) throws FooConfigurationException
	{
		parse(cfgSource, "");
	}


	public void parse(String cfgSource, String scope)
											throws FooConfigurationException
	{
		this.scope = scope;
		try {
			if (cfgSource != null && !cfgSource.equals("")) {
				cfg.parse(cfgSource);
			}
			cfg.setFallbackConfiguration(Configuration.INPUT_STRING,
										  FallbackConfiguration.getString());
		} catch(ConfigurationException ex) {
			throw new FooConfigurationException(ex.getMessage());
		}
	}


	//--------
	// Lookup-style functions.
	//--------
	public String lookupString(String name) throws FooConfigurationException
	{
		try {
			return cfg.lookupString(scope, name);
		} catch(ConfigurationException ex) {
			throw new FooConfigurationException(ex.getMessage());
		}
	}


	public String[] lookupList(String name) throws FooConfigurationException
	{
		try {
			return cfg.lookupList(scope, name);
		} catch(ConfigurationException ex) {
			throw new FooConfigurationException(ex.getMessage());
		}
	}


	public int lookupInt(String name) throws FooConfigurationException
	{
		try {
			return cfg.lookupInt(scope, name);
		} catch(ConfigurationException ex) {
			throw new FooConfigurationException(ex.getMessage());
		}
	}


	public float lookupFloat(String name) throws FooConfigurationException
	{
		try {
			return cfg.lookupFloat(scope, name);
		} catch(ConfigurationException ex) {
			throw new FooConfigurationException(ex.getMessage());
		}
	}


	public boolean lookupBoolean(String name) throws FooConfigurationException
	{
		try {
			return cfg.lookupBoolean(scope, name);
		} catch(ConfigurationException ex) {
			throw new FooConfigurationException(ex.getMessage());
		}
	}


	public int lookupDurationMilliseconds(String name)
											throws FooConfigurationException
	{
		try {
			return cfg.lookupDurationMilliseconds(scope, name);
		} catch(ConfigurationException ex) {
			throw new FooConfigurationException(ex.getMessage());
		}
	}


	public int lookupDurationSeconds(String name)
											throws FooConfigurationException
	{
		try {
			return cfg.lookupDurationSeconds(scope, name);
		} catch(ConfigurationException ex) {
			throw new FooConfigurationException(ex.getMessage());
		}
	}


	//--------
	// Instance variables
	//--------
	private String			scope;
	private Configuration	cfg;

}
