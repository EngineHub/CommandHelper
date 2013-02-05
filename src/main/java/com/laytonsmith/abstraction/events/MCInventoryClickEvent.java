package com.laytonsmith.abstraction.events;

import java.util.List;

import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryView;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.enums.MCSlotType;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author jb_aero
 */
public interface MCInventoryClickEvent extends BindableEvent {
	public MCItemStack getCurrentItem();
	public MCItemStack getCursor();
	public int getSlot();
	public MCSlotType getSlotType();
	public MCHumanEntity getWhoClicked();
	
	//from InventoryEvent
	public List<MCHumanEntity> getViewers();
	public MCInventoryView getView();
	public MCInventory getInventory();
	
	public boolean isLeftClick();
	public boolean isRightClick();
	public boolean isShiftClick();
	
	public void setCurrentItem(MCItemStack slot);
	public void setCursor(MCItemStack cursor);
}
