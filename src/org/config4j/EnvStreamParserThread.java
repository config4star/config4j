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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Properties;


class EnvStreamParserThread extends Thread
{
	EnvStreamParserThread(InputStream inputStream)
	{
		this.inputStream = inputStream;
		this.envProperties = new Properties();
	}

	Properties getEnvProperties()
	{
		return envProperties;
	}

	public void run()
	{
		InputStreamReader	isr;
		BufferedReader		br;
		String				line;
		String				key;
		String				value;

		int					index;
		try {
			isr = new InputStreamReader(inputStream);
			br = new BufferedReader(isr);
			while ((line = br.readLine()) != null) {
				index = line.indexOf("=");
				if (index != -1) {
					key   = line.substring(0, index).toUpperCase();
					value = line.substring(index+1);
					envProperties.setProperty(key, value);
				}
			}
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	private InputStream	inputStream;
	private Properties	envProperties;
}
