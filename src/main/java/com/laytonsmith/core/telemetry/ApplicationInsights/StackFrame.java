/*
* ApplicationInsights-Java
* Copyright (c) Microsoft Corporation
* All rights reserved.
*
* MIT License
* Permission is hereby granted, free of charge, to any person obtaining a copy of this
* software and associated documentation files (the ""Software""), to deal in the Software
* without restriction, including without limitation the rights to use, copy, modify, merge,
* publish, distribute, sublicense, and/or sell copies of the Software, and to permit
* persons to whom the Software is furnished to do so, subject to the following conditions:
* The above copyright notice and this permission notice shall be included in all copies or
* substantial portions of the Software.
* THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
* DEALINGS IN THE SOFTWARE.
*/
/*
 * Generated from StackFrame.bond (https://github.com/Microsoft/bond)
 */
package com.laytonsmith.core.telemetry.ApplicationInsights;

/**
 * Data contract class StackFrame.
 */
public final class StackFrame {

	/**
	 * Backing field for property Level.
	 */
	private int level;

	/**
	 * Backing field for property Method.
	 */
	private String method;

	/**
	 * Backing field for property Assembly.
	 */
	private String assembly;

	/**
	 * Backing field for property FileName.
	 */
	private String fileName;

	/**
	 * Backing field for property Line.
	 */
	private int line;

	/**
	 * Gets the Level property.
	 *
	 * @return
	 */
	public int getLevel() {
		return this.level;
	}

	/**
	 * Sets the Level property.
	 *
	 * @param value
	 */
	public void setLevel(int value) {
		this.level = value;
	}

	/**
	 * Gets the Method property.
	 *
	 * @return
	 */
	public String getMethod() {
		return this.method;
	}

	/**
	 * Sets the Method property.
	 *
	 * @param value
	 */
	public void setMethod(String value) {
		this.method = value;
	}

	/**
	 * Gets the Assembly property.
	 *
	 * @return
	 */
	public String getAssembly() {
		return this.assembly;
	}

	/**
	 * Sets the Assembly property.
	 *
	 * @param value
	 */
	public void setAssembly(String value) {
		this.assembly = value;
	}

	/**
	 * Gets the FileName property.
	 *
	 * @return
	 */
	public String getFileName() {
		return this.fileName;
	}

	/**
	 * Sets the FileName property.
	 *
	 * @param value
	 */
	public void setFileName(String value) {
		this.fileName = value;
	}

	/**
	 * Gets the Line property.
	 *
	 * @return
	 */
	public int getLine() {
		return this.line;
	}

	/**
	 * Sets the Line property.
	 *
	 * @param value
	 */
	public void setLine(int value) {
		this.line = value;
	}

}
