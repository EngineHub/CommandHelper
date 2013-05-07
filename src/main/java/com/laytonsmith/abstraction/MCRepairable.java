package com.laytonsmith.abstraction;

/**
 *
 * @author jb_aero
 */
public interface MCRepairable extends AbstractionObject {
	
	public boolean hasRepairCost();
	public int getRepairCost();
	public void setRepairCost(int cost);
	
}
