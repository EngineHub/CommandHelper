package com.laytonsmith.abstraction;

public class MCProfileProperty {

	private final String name;
	private final String value;
	private final String signature;

	public MCProfileProperty(String name, String value, String signature) {
		this.name = name;
		this.value = value;
		this.signature = signature;
	}

	public String getName() {
		return this.name;
	}

	public String getValue() {
		return this.value;
	}

	public String getSignature() {
		return this.signature;
	}

}
