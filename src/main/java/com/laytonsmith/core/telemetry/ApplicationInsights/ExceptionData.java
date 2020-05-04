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

import com.laytonsmith.PureUtilities.MapBuilder;
import java.util.ArrayList;
import java.util.List;
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
	private List<ExceptionDetails> exceptions;

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
	private ConcurrentMap<String, String> properties;

	/**
	 * Backing field for property Measurements.
	 */
	private ConcurrentMap<String, Double> measurements;

	/**
	 * Initializes a new instance of the ExceptionData class.
	 */
	@SuppressWarnings("OverridableMethodCallInConstructor")
	public ExceptionData() {
		this.InitializeFields();
	}

	/**
	 * Gets the Ver property.
	 * @return
	 */
	public int getVer() {
		return this.ver;
	}

	/**
	 * Sets the Ver property.
	 * @param value
	 */
	public void setVer(int value) {
		this.ver = value;
	}

	/**
	 * Gets the Exceptions property.
	 * @return
	 */
	public List<ExceptionDetails> getExceptions() {
		if(this.exceptions == null) {
			this.exceptions = new ArrayList<>();
		}
		return this.exceptions;
	}

	/**
	 * Sets the Exceptions property.
	 * @param value
	 */
	public void setExceptions(List<ExceptionDetails> value) {
		this.exceptions = value;
	}

	/**
	 * Gets the SeverityLevel property.
	 * @return
	 */
	public SeverityLevel getSeverityLevel() {
		return this.severityLevel;
	}

	/**
	 * Sets the SeverityLevel property.
	 * @param value
	 */
	public void setSeverityLevel(SeverityLevel value) {
		this.severityLevel = value;
	}

	/**
	 * Gets the ProblemId property.
	 * @return
	 */
	public String getProblemId() {
		return this.problemId;
	}

	/**
	 * Sets the ProblemId property.
	 * @param value
	 */
	public void setProblemId(String value) {
		this.problemId = value;
	}

	/**
	 * Gets the Properties property.
	 * @return
	 */
	public ConcurrentMap<String, String> getProperties() {
		if(this.properties == null) {
			this.properties = new ConcurrentHashMap<>();
		}
		return this.properties;
	}

	/**
	 * Sets the Properties property.
	 * @param value
	 */
	public void setProperties(ConcurrentMap<String, String> value) {
		this.properties = value;
	}

	/**
	 * Gets the Measurements property.
	 * @return
	 */
	public ConcurrentMap<String, Double> getMeasurements() {
		if(this.measurements == null) {
			this.measurements = new ConcurrentHashMap<>();
		}
		return this.measurements;
	}

	/**
	 * Sets the Measurements property.
	 * @param value
	 */
	public void setMeasurements(ConcurrentMap<String, Double> value) {
		this.measurements = value;
	}

	/**
	 * Serializes the beginning of this object to the passed in writer.
	 *
	 * @return
	 */
	@Override
	public Map<String, Object> serializeSubclass() {
		MapBuilder<String, Object> builder = MapBuilder.empty(String.class, Object.class)
			.set("ver", ver)
			.set("exceptions", exceptions)
			.set("severityLevel", severityLevel)
			.set("problemId", problemId, 1024)
			.set("properties", properties)
			.set("measurements", measurements);
		return builder.build();
	}

	/**
	 * Optionally initializes fields for the current context.
	 */
	@Override
	protected void InitializeFields() {

	}
}
