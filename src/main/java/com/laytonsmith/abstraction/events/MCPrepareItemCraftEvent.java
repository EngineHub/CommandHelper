package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCCraftingInventory;
import com.laytonsmith.abstraction.MCRecipe;
import com.laytonsmith.abstraction.events.MCInventoryEvent;

public interface MCPrepareItemCraftEvent extends MCInventoryEvent {
	public MCRecipe getRecipe();
	public boolean isRepair();
	public MCCraftingInventory getInventory();
}
