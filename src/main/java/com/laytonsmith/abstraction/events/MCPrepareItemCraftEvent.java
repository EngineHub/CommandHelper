package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCCraftingInventory;
import com.laytonsmith.abstraction.MCRecipe;

public interface MCPrepareItemCraftEvent extends MCInventoryEvent {
	public MCRecipe getRecipe();
	public boolean isRepair();
	@Override
	public MCCraftingInventory getInventory();
}
