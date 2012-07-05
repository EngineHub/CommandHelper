package com.laytonsmith.abstraction.bukkit;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.Plugin;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCMetadataValue;
import com.laytonsmith.abstraction.MCMetadatable;
import com.laytonsmith.abstraction.MCPlugin;

public class BukkitMCMetadatable implements MCMetadatable {
	Metadatable meta;
	public BukkitMCMetadatable(Metadatable m) {
		this.meta = m;
	}
	
	public BukkitMCMetadatable(AbstractionObject a) {
		if (a instanceof MCMetadatable) {
			this.meta = (Metadatable)a.getHandle();
		}
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

	public Object getHandle() {
		return meta;
	}

}
