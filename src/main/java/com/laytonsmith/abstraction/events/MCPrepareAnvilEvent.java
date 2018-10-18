package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

public interface MCPrepareAnvilEvent extends MCInventoryEvent {

	MCItemStack getResult();

	AnvilInventory getAnvilInventory();

	void setResult(ItemStack result);

}
