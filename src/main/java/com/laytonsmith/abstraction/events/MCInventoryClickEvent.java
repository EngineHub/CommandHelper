package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCHumanEntity;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.enums.MCClickType;
import com.laytonsmith.abstraction.enums.MCInventoryAction;
import com.laytonsmith.abstraction.enums.MCSlotType;

/**
 *
 * @author jb_aero
 */
public interface MCInventoryClickEvent extends MCInventoryInteractEvent {
	public MCInventoryAction getAction();
	public MCClickType getClickType();
	
	public MCItemStack getCurrentItem();
	public MCItemStack getCursor();
	public int getSlot();
	public int getRawSlot();
	public MCSlotType getSlotType();
	public MCHumanEntity getWhoClicked();

	public boolean isLeftClick();
	public boolean isRightClick();
	public boolean isShiftClick();
	public boolean isCreativeClick();
	public boolean isKeyboardClick();

	public void setCurrentItem(MCItemStack slot);
	public void setCursor(MCItemStack cursor);
}
