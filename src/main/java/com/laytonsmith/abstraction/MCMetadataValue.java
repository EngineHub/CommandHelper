package com.laytonsmith.abstraction;

public interface MCMetadataValue {

	boolean asBoolean();

	byte asByte();

	double asDouble();

	float asFloat();

	int asInt();

	long asLong();

	short asShort();

	String asString();

	MCPlugin getOwningPlugin();

	void invalidate();

	Object value();

	Object getHandle();
}
