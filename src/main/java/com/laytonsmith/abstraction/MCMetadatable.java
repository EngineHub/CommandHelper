package com.laytonsmith.abstraction;

import java.util.List;

public interface MCMetadatable extends AbstractionObject {
	List<MCMetadataValue> getMetadata(String metadataKey);
	boolean hasMetadata(String metadataKey);
	void removeMetadata(String metadataKey, MCPlugin owningPlugin);
	void setMetadata(String metadataKey, MCMetadataValue newMetadataValue);
}
