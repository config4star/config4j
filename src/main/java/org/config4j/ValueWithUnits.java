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

public class ValueWithUnits {
	private final static int FLOAT_VALUE = 2;
	private final static int INT_VALUE = 1;
	private final static int NO_VALUE = 0;

	private float floatValue;
	private int intValue;
	private final int state;
	private String units;

	public ValueWithUnits() {
		state = NO_VALUE;
	}

	public ValueWithUnits(float value, String units) {
		state = FLOAT_VALUE;
		floatValue = value;
		this.units = units;
	}

	public ValueWithUnits(int value, String units) {
		state = INT_VALUE;
		intValue = value;
		this.units = units;
	}

	public float getFloatValue() {
		if (state != FLOAT_VALUE) {
			throw new IllegalStateException("ValueWithUnits.getFloatValue()");
		}
		return floatValue;
	}

	public int getIntValue() {
		if (state != INT_VALUE) {
			throw new IllegalStateException("ValueWithUnits.getIntValue()");
		}
		return intValue;
	}

	public String getUnits() {
		if (state == NO_VALUE) {
			throw new IllegalStateException("ValueWithUnits.getUnits()");
		}
		return units;
	}
}
