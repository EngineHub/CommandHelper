package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCMetadataValue;
import com.laytonsmith.abstraction.MCMetadatable;
import com.laytonsmith.abstraction.MCPlugin;
import com.laytonsmith.annotations.WrappedItem;
import com.laytonsmith.annotations.testing.AbstractConstructor;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

public class BukkitMCMetadatable implements MCMetadatable {
	@WrappedItem Metadatable meta;
	public BukkitMCMetadatable(Metadatable m) {
		this.meta = m;
	}
	
	@AbstractConstructor
	public BukkitMCMetadatable(AbstractionObject a) {
		this.meta = a.getHandle();
	}

	public List<MCMetadataValue> getMetadata(String metadataKey) {
		List<MetadataValue> lst = meta.getMetadata(metadataKey);
		List<MCMetadataValue> retn = new ArrayList<MCMetadataValue>();
		
		for(MetadataValue val : lst) {
			retn.add(new BukkitMCMetadataValue(val));
		}
		
		return retn;
	}

	public boolean hasMetadata(String metadataKey) {
		return meta.hasMetadata(metadataKey);
	}

	public void removeMetadata(String metadataKey, MCPlugin owningPlugin) {
		meta.removeMetadata(metadataKey, ((BukkitMCPlugin)owningPlugin).getPlugin());
	}

	public void setMetadata(String metadataKey, MCMetadataValue newMetadataValue) {
		meta.setMetadata(metadataKey, ((BukkitMCMetadataValue)newMetadataValue).asMetadataValue());
	}

	public Metadatable getHandle() {
		return meta;
	}
	
	@Override
	public String toString() {
		return meta.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BukkitMCMetadatable?meta.equals(((BukkitMCMetadatable)obj).meta):false);
	}

	@Override
	public int hashCode() {
		return meta.hashCode();
	}

}
