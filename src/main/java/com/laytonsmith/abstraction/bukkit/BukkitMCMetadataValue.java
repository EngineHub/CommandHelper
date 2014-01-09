package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCMetadataValue;
import com.laytonsmith.abstraction.MCPlugin;
import org.bukkit.metadata.MetadataValue;

public class BukkitMCMetadataValue implements MCMetadataValue {

	MetadataValue val;
	
	public BukkitMCMetadataValue(MetadataValue v) {
		this.val = v;
	}
	@Override
	public boolean asBoolean() {
		return val.asBoolean();
	}

	@Override
	public byte asByte() {
		return val.asByte();
	}

	@Override
	public double asDouble() {
		return val.asDouble();
	}

	@Override
	public float asFloat() {
		return val.asFloat();
	}

	@Override
	public int asInt() {
		return val.asInt();
	}

	@Override
	public long asLong() {
		return val.asLong();
	}

	@Override
	public short asShort() {
		return val.asShort();
	}

	@Override
	public String asString() {
		return val.asString();
	}

	@Override
	public MCPlugin getOwningPlugin() {
		return new BukkitMCPlugin(val.getOwningPlugin());
	}

	@Override
	public void invalidate() {
		val.invalidate();
	}

	@Override
	public Object value() {
		return val.value();
	}
	@Override
	public MetadataValue getHandle() {
		return val;
	}
    public MetadataValue asMetadataValue() {
        return val;
    }

}
