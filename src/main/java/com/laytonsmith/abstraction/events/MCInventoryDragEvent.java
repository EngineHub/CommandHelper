package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.enums.MCDragType;
import java.util.Map;
import java.util.Set;

public interface MCInventoryDragEvent extends MCInventoryInteractEvent {

	Map<Integer, MCItemStack> getNewItems();

	Set<Integer> getRawSlots();

	Set<Integer> getInventorySlots();

	MCItemStack getCursor();

	void setCursor(MCItemStack newCursor);

	MCItemStack getOldCursor();

	MCDragType getType();
}
