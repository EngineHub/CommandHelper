package com.laytonsmith.abstraction;

public interface MCMetadataValue extends AbstractionObject {
	public boolean asBoolean();

	public byte asByte();

	public double asDouble();

	public float asFloat();

	public int asInt();

	public long asLong();

	public short asShort();

	public String asString();

	public MCPlugin getOwningPlugin();

	public void invalidate();

	public Object value();
}
