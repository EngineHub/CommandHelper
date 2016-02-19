package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.enums.MCDragType;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author import
 */
public interface MCInventoryDragEvent extends MCInventoryInteractEvent {
	public Map<Integer, MCItemStack> getNewItems();
	public Set<Integer> getRawSlots();
	public Set<Integer> getInventorySlots();
	public MCItemStack getCursor();
	public void setCursor(MCItemStack newCursor);
	public MCItemStack getOldCursor();
	public MCDragType getType();
}
