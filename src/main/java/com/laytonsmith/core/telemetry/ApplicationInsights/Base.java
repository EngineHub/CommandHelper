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
 * Generated from Base.bond (https://github.com/Microsoft/bond)
 */
package com.laytonsmith.core.telemetry.ApplicationInsights;


import com.laytonsmith.PureUtilities.JSONUtil;
import com.laytonsmith.PureUtilities.MapBuilder;

/**
 * Data contract class Base.
 */
public abstract class Base {

	/**
	 * Backing field for property BaseType.
	 */
	private String baseType;

	/**
	 * Initializes a new instance of the Base class.
	 */
	public Base() {
		this.InitializeFields();
	}

	/**
	 * Gets the BaseType property.
	 * @return
	 */
	public String getBaseType() {
		return this.baseType;
	}

	/**
	 * Sets the BaseType property.
	 * @param value
	 */
	public void setBaseType(String value) {
		this.baseType = value;
	}

	/**
	 * Serializes the beginning of this object to the passed in writer.
	 *
	 * @return
	 */
//	@Override
//	public String serialize() {
//		MapBuilder<String, Object> builder = serializeSubclass();
//		builder.set("baseType", getBaseType());
//		return new JSONUtil().serialize(builder.build());
//	}

	protected abstract MapBuilder<String, Object> serializeSubclass();

	/**
	 * Optionally initializes fields for the current context.
	 */
	protected void InitializeFields() {

	}
}
