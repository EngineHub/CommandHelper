package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCMetadataValue;
import com.laytonsmith.abstraction.MCMetadatable;
import com.laytonsmith.abstraction.MCPlugin;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

public class BukkitMCMetadatable implements MCMetadatable {

	private final Metadatable metadatable;

	public BukkitMCMetadatable(Metadatable metadatable) {
		this.metadatable = metadatable;
	}

	@Override
	public List<MCMetadataValue> getMetadata(String metadataKey) {
		List<MetadataValue> lst = this.metadatable.getMetadata(metadataKey);
		List<MCMetadataValue> retn = new ArrayList<>();
		for(MetadataValue val : lst) {
			retn.add(new BukkitMCMetadataValue(val));
		}
		return retn;
	}

	@Override
	public boolean hasMetadata(String metadataKey) {
		return this.metadatable.hasMetadata(metadataKey);
	}

	@Override
	public void removeMetadata(String metadataKey, MCPlugin owningPlugin) {
		this.metadatable.removeMetadata(metadataKey, ((BukkitMCPlugin) owningPlugin).getHandle());
	}

	@Override
	public void setMetadata(String metadataKey, MCMetadataValue newMetadataValue) {
		this.metadatable.setMetadata(metadataKey, ((BukkitMCMetadataValue) newMetadataValue).getHandle());
	}

	@Override
	public Metadatable getHandle() {
		return this.metadatable;
	}

	@Override
	public String toString() {
		return this.metadatable.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof BukkitMCMetadatable && this.metadatable.equals(((BukkitMCMetadatable) obj).metadatable);
	}

	@Override
	public int hashCode() {
		return this.metadatable.hashCode();
	}
}
