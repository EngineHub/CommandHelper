package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryHolder;
import org.bukkit.inventory.InventoryHolder;

/**
 *
 * @author import
 */
public class BukkitMCInventoryHolder implements MCInventoryHolder {
	InventoryHolder holder;
	
	public BukkitMCInventoryHolder(InventoryHolder i) {
		holder = i;
	}
	
	public MCInventory getInventory() {
		return new BukkitMCInventory(holder.getInventory());
	}

	public Object getHandle() {
		return holder;
	}
	
}
