package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCMetadataValue;
import com.laytonsmith.abstraction.MCMetadatable;
import com.laytonsmith.abstraction.MCPlugin;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

public class BukkitMCMetadatable implements MCMetadatable {

	private final Metadatable m_metadatable;

	public BukkitMCMetadatable(Metadatable metadatable) {
		m_metadatable = metadatable;
	}

	@Override
	public List<MCMetadataValue> getMetadata(String metadataKey) {
		List<MetadataValue> lst = m_metadatable.getMetadata(metadataKey);
		List<MCMetadataValue> retn = new ArrayList<>();
		
		for(MetadataValue val : lst) {
			retn.add(new BukkitMCMetadataValue(val));
		}
		
		return retn;
	}

	@Override
	public boolean hasMetadata(String metadataKey) {
		return m_metadatable.hasMetadata(metadataKey);
	}

	@Override
	public void removeMetadata(String metadataKey, MCPlugin owningPlugin) {
		m_metadatable.removeMetadata(metadataKey, ((BukkitMCPlugin)owningPlugin).getHandle());
	}

	@Override
	public void setMetadata(String metadataKey, MCMetadataValue newMetadataValue) {
		m_metadatable.setMetadata(metadataKey, ((BukkitMCMetadataValue)newMetadataValue).getHandle());
	}

	@Override
	public Metadatable getHandle() {
		return m_metadatable;
	}

	@Override
	public String toString() {
		return m_metadatable.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BukkitMCMetadatable?m_metadatable.equals(((BukkitMCMetadatable)obj).m_metadatable):false);
	}

	@Override
	public int hashCode() {
		return m_metadatable.hashCode();
	}
}