package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCMetadataValue;
import com.laytonsmith.abstraction.MCPlugin;
import org.bukkit.metadata.MetadataValue;

public class BukkitMCMetadataValue implements MCMetadataValue {

	private final MetadataValue _value;

	public BukkitMCMetadataValue(MetadataValue value) {
		_value = value;
	}

	@Override
	public boolean asBoolean() {
		return _value.asBoolean();
	}

	@Override
	public byte asByte() {
		return _value.asByte();
	}

	@Override
	public double asDouble() {
		return _value.asDouble();
	}

	@Override
	public float asFloat() {
		return _value.asFloat();
	}

	@Override
	public int asInt() {
		return _value.asInt();
	}

	@Override
	public long asLong() {
		return _value.asLong();
	}

	@Override
	public short asShort() {
		return _value.asShort();
	}

	@Override
	public String asString() {
		return _value.asString();
	}

	@Override
	public MCPlugin getOwningPlugin() {
		return new BukkitMCPlugin(_value.getOwningPlugin());
	}

	@Override
	public void invalidate() {
		_value.invalidate();
	}

	@Override
	public Object value() {
		return _value.value();
	}

	@Override
	public MetadataValue getHandle() {
		return _value;
	}
}