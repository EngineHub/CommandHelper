package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCMetadataValue;
import com.laytonsmith.abstraction.MCMetadatable;
import com.laytonsmith.abstraction.MCPlugin;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

public class BukkitMCMetadatable implements MCMetadatable {

	private final Metadatable _metadatable;

	public BukkitMCMetadatable(Metadatable metadatable) {
		_metadatable = metadatable;
	}

	@Override
	public List<MCMetadataValue> getMetadata(String metadataKey) {
		List<MetadataValue> lst = _metadatable.getMetadata(metadataKey);
		List<MCMetadataValue> retn = new ArrayList<>();
		
		for(MetadataValue val : lst) {
			retn.add(new BukkitMCMetadataValue(val));
		}
		
		return retn;
	}

	@Override
	public boolean hasMetadata(String metadataKey) {
		return _metadatable.hasMetadata(metadataKey);
	}

	@Override
	public void removeMetadata(String metadataKey, MCPlugin owningPlugin) {
		_metadatable.removeMetadata(metadataKey, ((BukkitMCPlugin)owningPlugin).getHandle());
	}

	@Override
	public void setMetadata(String metadataKey, MCMetadataValue newMetadataValue) {
		_metadatable.setMetadata(metadataKey, ((BukkitMCMetadataValue)newMetadataValue).getHandle());
	}

	@Override
	public Metadatable getHandle() {
		return _metadatable;
	}

	@Override
	public String toString() {
		return _metadatable.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BukkitMCMetadatable?_metadatable.equals(((BukkitMCMetadatable)obj)._metadatable):false);
	}

	@Override
	public int hashCode() {
		return _metadatable.hashCode();
	}
}