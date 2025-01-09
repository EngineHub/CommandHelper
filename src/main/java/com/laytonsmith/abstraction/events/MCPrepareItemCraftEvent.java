package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCCraftingInventory;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCRecipe;

public interface MCPrepareItemCraftEvent extends MCInventoryEvent {
	MCPlayer getPlayer();

	MCRecipe getRecipe();

	boolean isRepair();

	@Override
	MCCraftingInventory getInventory();
}
