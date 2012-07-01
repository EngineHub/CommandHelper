package com.laytonsmith.abstraction;

import java.util.List;

public interface MCMetadatable {
	public List<MCMetadataValue> getMetadata(String metadataKey);

	public boolean hasMetadata(String metadataKey);

	public void removeMetadata(String metadataKey, MCPlugin owningPlugin);

	public void setMetadata(String metadataKey, MCMetadataValue newMetadataValue);
}
