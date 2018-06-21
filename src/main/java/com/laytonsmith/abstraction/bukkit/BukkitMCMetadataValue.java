package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCMetadataValue;
import com.laytonsmith.abstraction.MCPlugin;
import org.bukkit.metadata.MetadataValue;

public class BukkitMCMetadataValue implements MCMetadataValue {

	private final MetadataValue value;

	public BukkitMCMetadataValue(MetadataValue value) {
		this.value = value;
	}

	@Override
	public boolean asBoolean() {
		return this.value.asBoolean();
	}

	@Override
	public byte asByte() {
		return this.value.asByte();
	}

	@Override
	public double asDouble() {
		return this.value.asDouble();
	}

	@Override
	public float asFloat() {
		return this.value.asFloat();
	}

	@Override
	public int asInt() {
		return this.value.asInt();
	}

	@Override
	public long asLong() {
		return this.value.asLong();
	}

	@Override
	public short asShort() {
		return this.value.asShort();
	}

	@Override
	public String asString() {
		return this.value.asString();
	}

	@Override
	public MCPlugin getOwningPlugin() {
		return new BukkitMCPlugin(this.value.getOwningPlugin());
	}

	@Override
	public void invalidate() {
		this.value.invalidate();
	}

	@Override
	public Object value() {
		return this.value.value();
	}

	@Override
	public MetadataValue getHandle() {
		return this.value;
	}
}
