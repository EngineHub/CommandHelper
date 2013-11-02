package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.entities.MCHumanEntity;
import com.laytonsmith.abstraction.enums.MCInventoryType;

/**
 *
 * @author Layton
 */
public interface MCInventoryView {

	public MCInventory getBottomInventory();

	public MCInventory getTopInventory();

	public void close();

	public int countSlots();

	public int convertSlot(int rawSlot);

	public MCItemStack getItem(int slot);

	public MCHumanEntity getPlayer();

	public String getTitle();
	
	public MCInventoryType getType();

	public void setCursor(MCItemStack item);

	public void setItem(int slot, MCItemStack item);
}
