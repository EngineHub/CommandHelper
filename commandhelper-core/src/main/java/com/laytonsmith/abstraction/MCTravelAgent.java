package com.laytonsmith.abstraction;

/**
 *
 * @author MariuszT
 */
public interface MCTravelAgent extends AbstractionObject {

	public boolean createPortal(MCLocation location);
	public MCLocation findOrCreate(MCLocation location);
	public MCLocation findPortal(MCLocation location);

	public boolean getCanCreatePortal();
	public void setCanCreatePortal(boolean create);

	public int getCreationRadius();
	public MCTravelAgent setCreationRadius(int radius);

	public int getSearchRadius();
	public MCTravelAgent setSearchRadius(int radius);
}
