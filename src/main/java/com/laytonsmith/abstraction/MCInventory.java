package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCInventoryType;
import java.util.List;
import java.util.Map;

public interface MCInventory extends AbstractionObject {
	Map<Integer, MCItemStack> addItem(MCItemStack stack);
	MCInventoryType getType();
	int getSize();
	MCItemStack getItem(int index);
	void setItem(int index, MCItemStack stack);
	List<MCHumanEntity> getViewers();
	void updateViewers();
	void clear();
	void clear(int index);
	MCInventoryHolder getHolder();
	String getTitle();
}
