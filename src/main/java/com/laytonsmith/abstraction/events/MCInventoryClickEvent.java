package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.enums.MCSlotType;

/**
 *
 * @author jb_aero
 */
public interface MCInventoryClickEvent extends MCInventoryEvent {
	public MCItemStack getCurrentItem();
	public MCItemStack getCursor();
	public int getSlot();
	public int getRawSlot();
	public MCSlotType getSlotType();
	public MCHumanEntity getWhoClicked();

	public boolean isLeftClick();
	public boolean isRightClick();
	public boolean isShiftClick();

	public void setCurrentItem(MCItemStack slot);
	public void setCursor(MCItemStack cursor);
	public void setCancelled(boolean cancelled);
}
