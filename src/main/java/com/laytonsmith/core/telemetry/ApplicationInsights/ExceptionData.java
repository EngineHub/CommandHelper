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
 * Generated from ExceptionData.bond (https://github.com/Microsoft/bond)
 */
package com.laytonsmith.core.telemetry.ApplicationInsights;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Data contract class ExceptionData.
 */
public final class ExceptionData extends Domain {

	/**
	 * Backing field for property Ver.
	 */
	private int ver = 2;

	/**
	 * Backing field for property Exceptions.
	 */
	private ExceptionDetails[] exceptions;

	/**
	 * Backing field for property SeverityLevel.
	 */
	private SeverityLevel severityLevel;

	/**
	 * Backing field for property ProblemId.
	 */
	private String problemId;

	/**
	 * Backing field for property Properties.
	 */
	private Map<String, String> properties;

	/**
	 * Backing field for property Measurements.
	 */
	private Map<String, Double> measurements;

	/**
	 * Gets the Ver property.
	 *
	 * @return
	 */
	public int getVer() {
		return this.ver;
	}

	/**
	 * Sets the Ver property.
	 *
	 * @param value
	 */
	public void setVer(int value) {
		this.ver = value;
	}

	/**
	 * Gets the Exceptions property.
	 *
	 * @return
	 */
	public ExceptionDetails[] getExceptions() {
		if(this.exceptions == null) {
			this.exceptions = new ExceptionDetails[0];
		}
		return this.exceptions;
	}

	/**
	 * Sets the Exceptions property.
	 *
	 * @param value
	 */
	public void setExceptions(ExceptionDetails[] value) {
		this.exceptions = value;
	}

	/**
	 * Gets the SeverityLevel property.
	 *
	 * @return
	 */
	public SeverityLevel getSeverityLevel() {
		return this.severityLevel;
	}

	/**
	 * Sets the SeverityLevel property.
	 *
	 * @param value
	 */
	public void setSeverityLevel(SeverityLevel value) {
		this.severityLevel = value;
	}

	/**
	 * Gets the ProblemId property.
	 *
	 * @return
	 */
	public String getProblemId() {
		return this.problemId;
	}

	/**
	 * Sets the ProblemId property.
	 *
	 * @param value
	 */
	public void setProblemId(String value) {
		this.problemId = value;
	}

	/**
	 * Gets the Properties property.
	 *
	 * @return
	 */
	public Map<String, String> getProperties() {
		if(this.properties == null) {
			this.properties = new ConcurrentHashMap<>();
		}
		return this.properties;
	}

	/**
	 * Sets the Properties property.
	 *
	 * @param value
	 */
	public void setProperties(ConcurrentMap<String, String> value) {
		this.properties = value;
	}

	/**
	 * Gets the Measurements property.
	 *
	 * @return
	 */
	public Map<String, Double> getMeasurements() {
		if(this.measurements == null) {
			this.measurements = new ConcurrentHashMap<>();
		}
		return this.measurements;
	}

	/**
	 * Sets the Measurements property.
	 *
	 * @param value
	 */
	public void setMeasurements(ConcurrentMap<String, Double> value) {
		this.measurements = value;
	}

}
