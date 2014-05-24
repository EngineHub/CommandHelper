package com.laytonsmith.abstraction;

/**
 * 
 * @author jb_aero
 */
public interface MCSkullMeta extends MCItemMeta {
	
	public boolean hasOwner();
	public String getOwner();
	public boolean setOwner(String owner);
	
}
