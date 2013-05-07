package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionUtils;
import com.laytonsmith.abstraction.MCMetadataValue;
import com.laytonsmith.abstraction.MCPlugin;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.metadata.MetadataValue;

public class BukkitMCMetadataValue implements MCMetadataValue {

	@WrappedItem MetadataValue val;
	
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
		return AbstractionUtils.wrap(val.getOwningPlugin());
	}

	public void invalidate() {
		val.invalidate();
	}

	public Object value() {
		return val.value();
	}
	
	public <T> T getHandle() {
		return (T) val;
	}

}
