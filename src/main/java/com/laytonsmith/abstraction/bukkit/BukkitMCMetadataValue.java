package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCMetadataValue;
import com.laytonsmith.abstraction.MCPlugin;
import org.bukkit.metadata.MetadataValue;

public class BukkitMCMetadataValue implements MCMetadataValue {

	private final MetadataValue m_value;

	public BukkitMCMetadataValue(MetadataValue value) {
		m_value = value;
	}

	@Override
	public boolean asBoolean() {
		return m_value.asBoolean();
	}

	@Override
	public byte asByte() {
		return m_value.asByte();
	}

	@Override
	public double asDouble() {
		return m_value.asDouble();
	}

	@Override
	public float asFloat() {
		return m_value.asFloat();
	}

	@Override
	public int asInt() {
		return m_value.asInt();
	}

	@Override
	public long asLong() {
		return m_value.asLong();
	}

	@Override
	public short asShort() {
		return m_value.asShort();
	}

	@Override
	public String asString() {
		return m_value.asString();
	}

	@Override
	public MCPlugin getOwningPlugin() {
		return new BukkitMCPlugin(m_value.getOwningPlugin());
	}

	@Override
	public void invalidate() {
		m_value.invalidate();
	}

	@Override
	public Object value() {
		return m_value.value();
	}

	@Override
	public MetadataValue getHandle() {
		return m_value;
	}
}