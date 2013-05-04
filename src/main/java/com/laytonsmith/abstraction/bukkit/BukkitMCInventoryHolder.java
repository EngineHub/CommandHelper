package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryHolder;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.inventory.InventoryHolder;

/**
 *
 * @author import
 */
public class BukkitMCInventoryHolder implements MCInventoryHolder {
	@WrappedItem InventoryHolder holder;
	
	public BukkitMCInventoryHolder(InventoryHolder i) {
		holder = i;
	}
	
	public MCInventory getInventory() {
		return new BukkitMCInventory(holder.getInventory());
	}

	public Object getHandle() {
		return holder;
	}

	@Override
	public String toString() {
		return holder.toString();
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object obj) {
		return holder.equals(obj);
	}

	@Override
	public int hashCode() {
		return holder.hashCode();
	}
}
