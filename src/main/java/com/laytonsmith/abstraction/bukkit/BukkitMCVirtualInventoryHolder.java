package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCVirtualInventoryHolder;
import com.laytonsmith.core.functions.InventoryManagement;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class BukkitMCVirtualInventoryHolder implements MCVirtualInventoryHolder {

	private final VirtualHolder vholder;

	public BukkitMCVirtualInventoryHolder(String id, String title) {
		this.vholder = new VirtualHolder(id, title);
	}

	public BukkitMCVirtualInventoryHolder(InventoryHolder ih) {
		this.vholder = (VirtualHolder) ih;
	}

	@Override
	public MCInventory getInventory() {
		return new BukkitMCInventory(this.vholder.getInventory());
	}

	@Override
	public String getID() {
		return this.vholder.id;
	}

	@Override
	public VirtualHolder getHandle() {
		return this.vholder;
	}

	public static class VirtualHolder implements InventoryHolder {
		private final String id;
		private final String title;

		VirtualHolder(String id, String title) {
			this.id = id;
			this.title = title;
		}

		@Override
		public @NotNull Inventory getInventory() {
			return (Inventory) InventoryManagement.VIRTUAL_INVENTORIES.get(this.id).getHandle();
		}

		public String getTitle() {
			return this.title;
		}
	}
}
