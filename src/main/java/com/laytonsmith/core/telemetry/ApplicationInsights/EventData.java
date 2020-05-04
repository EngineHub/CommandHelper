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
 * Generated from EventData.bond (https://github.com/Microsoft/bond)
 */
package com.laytonsmith.core.telemetry.ApplicationInsights;

import com.laytonsmith.PureUtilities.MapBuilder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Data contract class EventData.
 */
public final class EventData extends Domain {

	/**
	 * Backing field for property Ver.
	 */
	private int ver = 2;

	/**
	 * Backing field for property Name.
	 */
	private String name;

	/**
	 * Backing field for property Properties.
	 */
	private ConcurrentMap<String, String> properties;

	/**
	 * Backing field for property Measurements.
	 */
	private ConcurrentMap<String, Double> measurements;

	/**
	 * Initializes a new instance of the EventData class.
	 */
	public EventData() {
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
	 * Gets the Name property.
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the Name property.
	 * @param value
	 */
	public void setName(String value) {
		this.name = value;
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
	protected Map<String, Object> serializeSubclass() {
		MapBuilder<String, Object> builder = MapBuilder.empty(String.class, Object.class)
			.set("ver", ver)
			.set("name", name)
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
