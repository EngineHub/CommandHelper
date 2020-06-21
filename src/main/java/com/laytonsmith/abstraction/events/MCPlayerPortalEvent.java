package com.laytonsmith.abstraction.events;

public interface MCPlayerPortalEvent extends MCPlayerTeleportEvent {

	int getSearchRadius();

	void setSearchRadius(int radius);

	int getCreationRadius();

	void setCreationRadius(int radius);

	boolean canCreatePortal();

	void setCanCreatePortal(boolean canCreate);
}
