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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class StreamReaderThread extends Thread {
	private StringBuffer buf;

	private InputStream inputStream;

	StreamReaderThread(InputStream inputStream) {
		this.inputStream = inputStream;
		buf = new StringBuffer();
	}

	String getBuf() {
		return buf.toString();
	}

	@Override
	public void run() {
		InputStreamReader isr;
		BufferedReader br;
		boolean first;
		String line;

		try {
			isr = new InputStreamReader(inputStream);
			br = new BufferedReader(isr);
			first = true;
			while ((line = br.readLine()) != null) {
				if (!first) {
					buf.append("\n");
				}
				buf.append(line);
				first = false;
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
