package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCVirtualInventoryHolder;
import com.laytonsmith.core.functions.InventoryManagement;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class BukkitMCVirtualInventoryHolder implements MCVirtualInventoryHolder {

	private VirtualHolder vholder;

	public BukkitMCVirtualInventoryHolder(String id) {
		this.vholder = new VirtualHolder(id);
	}

	public BukkitMCVirtualInventoryHolder(InventoryHolder ih) {
		this.vholder = (VirtualHolder) ih;
	}

	@Override
	public MCInventory getInventory() {
		return new BukkitMCInventory(vholder.getInventory());
	}

	@Override
	public String getID() {
		return vholder.id;
	}

	@Override
	public Object getHandle() {
		return this.vholder;
	}

	public class VirtualHolder implements InventoryHolder {
		private String id;

		VirtualHolder(String id) {
			this.id = id;
		}

		@Override
		public Inventory getInventory() {
			return (Inventory) InventoryManagement.VIRTUAL_INVENTORIES.get(this.id).getHandle();
		}
	}
}
