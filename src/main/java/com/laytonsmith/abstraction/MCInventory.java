package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCInventoryType;
import java.util.HashMap;
import java.util.List;

/**
 *
 */
public interface MCInventory extends AbstractionObject {
	public HashMap addItem(MCItemStack stack);
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
