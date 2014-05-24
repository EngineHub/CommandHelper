package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCInventoryType;
import java.util.List;
import java.util.Map;

/**
 *
 * 
 */
public interface MCInventory extends AbstractionObject {
	public Map<Integer, MCItemStack> addItem(MCItemStack stack);
	public MCInventoryType getType();
	public int getSize();
	public MCItemStack getItem(int index);
	public void setItem(int index, MCItemStack stack);
	public List<MCHumanEntity> getViewers();
	public void clear();
	public void clear(int index);
	public MCInventoryHolder getHolder();
	public String getTitle();
}
