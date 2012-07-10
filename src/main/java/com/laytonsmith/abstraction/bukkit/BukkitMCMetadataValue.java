package com.laytonsmith.abstraction.bukkit;

import org.bukkit.metadata.MetadataValue;

import com.laytonsmith.abstraction.MCMetadataValue;
import com.laytonsmith.abstraction.MCPlugin;

public class BukkitMCMetadataValue implements MCMetadataValue {

	MetadataValue val;
	
	public BukkitMCMetadataValue(MetadataValue v) {
		this.val = v;
	}
	public boolean asBoolean() {
		return val.asBoolean();
	}

	public byte asByte() {
		return val.asByte();
	}

	public double asDouble() {
		return val.asDouble();
	}

	public float asFloat() {
		return val.asFloat();
	}

	public int asInt() {
		return val.asInt();
	}

	public long asLong() {
		return val.asLong();
	}

	public short asShort() {
		return val.asShort();
	}

	public String asString() {
		return val.asString();
	}

	public MCPlugin getOwningPlugin() {
		return new BukkitMCPlugin(val.getOwningPlugin());
	}

	public void invalidate() {
		val.invalidate();
	}

	public Object value() {
		return val.value();
	}
	public MetadataValue getHandle() {
		return val;
	}
    public MetadataValue asMetadataValue() {
        return val;
    }

}
