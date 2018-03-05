package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.enums.MCClickType;
import com.laytonsmith.abstraction.enums.MCInventoryAction;
import com.laytonsmith.abstraction.enums.MCSlotType;

public interface MCInventoryClickEvent extends MCInventoryInteractEvent {

	MCInventoryAction getAction();

	MCClickType getClickType();

	MCItemStack getCurrentItem();

	MCItemStack getCursor();

	int getSlot();

	int getRawSlot();

	int getHotbarButton();

	MCSlotType getSlotType();

	@Override
	MCHumanEntity getWhoClicked();

	boolean isLeftClick();

	boolean isRightClick();

	boolean isShiftClick();

	boolean isCreativeClick();

	boolean isKeyboardClick();

	void setCurrentItem(MCItemStack slot);

	void setCursor(MCItemStack cursor);
}
