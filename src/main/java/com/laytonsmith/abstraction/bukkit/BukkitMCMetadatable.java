package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCMetadataValue;
import com.laytonsmith.abstraction.MCMetadatable;
import com.laytonsmith.abstraction.MCPlugin;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

public abstract class BukkitMCMetadatable implements MCMetadatable {

	protected Metadatable metadatable;

	public BukkitMCMetadatable(Metadatable m) {
		this.metadatable = m;
	}

	@Override
	public Metadatable getHandle() {
		return metadatable;
	}

	public List<MCMetadataValue> getMetadata(String metadataKey) {
		List<MetadataValue> lst = metadatable.getMetadata(metadataKey);
		List<MCMetadataValue> retn = new ArrayList<MCMetadataValue>();
		
		for(MetadataValue val : lst) {
			retn.add(new BukkitMCMetadataValue(val));
		}
		
		return retn;
	}

	public boolean hasMetadata(String metadataKey) {
		return metadatable.hasMetadata(metadataKey);
	}

	public void removeMetadata(String metadataKey, MCPlugin owningPlugin) {
		metadatable.removeMetadata(metadataKey, ((BukkitMCPlugin)owningPlugin).getPlugin());
	}

	public void setMetadata(String metadataKey, MCMetadataValue newMetadataValue) {
		metadatable.setMetadata(metadataKey, ((BukkitMCMetadataValue)newMetadataValue).asMetadataValue());
	}
	
	@Override
	public String toString() {
		return metadatable.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BukkitMCMetadatable?metadatable.equals(((BukkitMCMetadatable)obj).metadatable):false);
	}

	@Override
	public int hashCode() {
		return metadatable.hashCode();
	}
}