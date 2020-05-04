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
 * Generated from ExceptionDetails.bond (https://github.com/Microsoft/bond)
 */
package com.laytonsmith.core.telemetry.ApplicationInsights;

import com.laytonsmith.PureUtilities.JSONUtil;
import com.laytonsmith.PureUtilities.MapBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * Data contract class ExceptionDetails.
 */
public class ExceptionDetails {

	/**
	 * Backing field for property Id.
	 */
	private int id;

	/**
	 * Backing field for property OuterId.
	 */
	private int outerId;

	/**
	 * Backing field for property TypeName.
	 */
	private String typeName;

	/**
	 * Backing field for property Message.
	 */
	private String message;

	/**
	 * Backing field for property HasFullStack.
	 */
	private boolean hasFullStack = true;

	/**
	 * Backing field for property Stack.
	 */
	private String stack;

	/**
	 * Backing field for property ParsedStack.
	 */
	private List<StackFrame> parsedStack;

	/**
	 * Initializes a new instance of the ExceptionDetails class.
	 */
	@SuppressWarnings("OverridableMethodCallInConstructor")
	public ExceptionDetails() {
		this.InitializeFields();
	}

	/**
	 * Gets the Id property.
	 * @return
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * Sets the Id property.
	 * @param value
	 */
	public void setId(int value) {
		this.id = value;
	}

	/**
	 * Gets the OuterId property.
	 * @return
	 */
	public int getOuterId() {
		return this.outerId;
	}

	/**
	 * Sets the OuterId property.
	 * @param value
	 */
	public void setOuterId(int value) {
		this.outerId = value;
	}

	/**
	 * Gets the TypeName property.
	 * @return
	 */
	public String getTypeName() {
		return this.typeName;
	}

	/**
	 * Sets the TypeName property.
	 * @param value
	 */
	public void setTypeName(String value) {
		this.typeName = value;
	}

	/**
	 * Gets the Message property.
	 * @return
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * Sets the Message property.
	 * @param value
	 */
	public void setMessage(String value) {
		this.message = value;
	}

	/**
	 * Gets the HasFullStack property.
	 * @return
	 */
	public boolean getHasFullStack() {
		return this.hasFullStack;
	}

	/**
	 * Sets the HasFullStack property.
	 * @param value
	 */
	public void setHasFullStack(boolean value) {
		this.hasFullStack = value;
	}

	/**
	 * Gets the Stack property.
	 * @return
	 */
	public String getStack() {
		return this.stack;
	}

	/**
	 * Sets the Stack property.
	 * @param value
	 */
	public void setStack(String value) {
		this.stack = value;
	}

	/**
	 * Gets the ParsedStack property.
	 * @return
	 */
	public List<StackFrame> getParsedStack() {
		if(this.parsedStack == null) {
			this.parsedStack = new ArrayList<>();
		}
		return this.parsedStack;
	}

	/**
	 * Sets the ParsedStack property.
	 * @param value
	 */
	public void setParsedStack(List<StackFrame> value) {
		this.parsedStack = value;
	}

	/**
	 * Serializes the beginning of this object to the passed in writer.
	 *
	 * @return
	 */
//	@Override
//	public String serialize() {
//		MapBuilder<String, Object> builder = MapBuilder.empty(String.class, Object.class)
//			.set("id", id)
//			.set("outerId", outerId)
//			.set("typeName", typeName)
//			.set("message", message)
//			.set("hasFullStack", hasFullStack)
//			.set("stack", stack)
//			.set("parsedStack", parsedStack);
//		return new JSONUtil().serialize(builder.build());
//	}

	/**
	 * Optionally initializes fields for the current context.
	 */
	protected void InitializeFields() {

	}
}
