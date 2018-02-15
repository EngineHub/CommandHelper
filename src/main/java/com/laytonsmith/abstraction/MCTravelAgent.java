package com.laytonsmith.abstraction;

public interface MCTravelAgent extends AbstractionObject {
	boolean createPortal(MCLocation location);
	MCLocation findOrCreate(MCLocation location);
	MCLocation findPortal(MCLocation location);

	boolean getCanCreatePortal();
	void setCanCreatePortal(boolean create);

	int getCreationRadius();
	MCTravelAgent setCreationRadius(int radius);

	int getSearchRadius();
	MCTravelAgent setSearchRadius(int radius);
}
