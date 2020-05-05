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
 * This has been ripped from the Application Insights SDK.
 */
package com.laytonsmith.core.telemetry.ApplicationInsights;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data contract class Envelope.
 */
public final class Envelope {

	/**
	 * Backing field for property Ver.
	 */
	private int ver = 1;

	/**
	 * Backing field for property Name.
	 */
	private String name;

	/**
	 * Backing field for property Time.
	 */
	private String time;

	/**
	 * Backing field for property SampleRate.
	 */
	private double sampleRate = 100.0;

	/**
	 * Backing field for property Seq.
	 */
	private String seq;

	/**
	 * Backing field for property IKey.
	 */
	private String iKey;

	/**
	 * Backing field for property Tags.
	 */
	private Map<String, String> tags;

	/**
	 * Backing field for property Data.
	 */
	private Base data;

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
	 * Gets the Name property.
	 *
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the Name property.
	 *
	 * @param value
	 */
	public void setName(String value) {
		this.name = value;
	}

	/**
	 * Gets the Time property.
	 *
	 * @return
	 */
	public String getTime() {
		return this.time;
	}

	/**
	 * Sets the Time property.
	 *
	 * @param value
	 */
	public void setTime(String value) {
		this.time = value;
	}

	/**
	 * Gets the SampleRate property.
	 *
	 * @return
	 */
	public double getSampleRate() {
		return this.sampleRate;
	}

	/**
	 * Sets the SampleRate property.
	 *
	 * @param value
	 */
	public void setSampleRate(double value) {
		this.sampleRate = value;
	}

	/**
	 * Gets the Seq property.
	 *
	 * @return
	 */
	public String getSeq() {
		return this.seq;
	}

	/**
	 * Sets the Seq property.
	 *
	 * @param value
	 */
	public void setSeq(String value) {
		this.seq = value;
	}

	/**
	 * Gets the IKey property.
	 *
	 * @return
	 */
	public String getIKey() {
		return this.iKey;
	}

	/**
	 * Sets the IKey property.
	 *
	 * @param value
	 */
	public void setIKey(String value) {
		this.iKey = value;
	}

	/**
	 * Gets the Tags property.
	 *
	 * @return
	 */
	public Map<String, String> getTags() {
		if(this.tags == null) {
			this.tags = new ConcurrentHashMap<>();
		}
		return this.tags;
	}

	/**
	 * Sets the Tags property. See available options here:
	 * https://github.com/microsoft/ApplicationInsights-Home/blob/master/EndpointSpecs/Schemas/Bond/ContextTagKeys.bond
	 *
	 * @param value
	 */
	public void setTags(Map<String, String> value) {
		this.tags = value;
	}

	/**
	 * Gets the Data property.
	 *
	 * @return
	 */
	public Base getData() {
		return this.data;
	}

	/**
	 * Sets the Data property.
	 *
	 * @param value
	 */
	public void setData(Base value) {
		this.data = value;
	}
}
