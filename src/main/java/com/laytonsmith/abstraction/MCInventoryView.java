package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCInventoryType;

public interface MCInventoryView {

	MCInventory getBottomInventory();

	MCInventory getTopInventory();

	void close();

	int countSlots();

	int convertSlot(int rawSlot);

	MCItemStack getItem(int slot);

	MCHumanEntity getPlayer();

	String getTitle();

	MCInventoryType getType();

	void setCursor(MCItemStack item);

	void setItem(int slot, MCItemStack item);
}
