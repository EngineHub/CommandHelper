package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCVirtualInventoryHolder;
import com.laytonsmith.core.functions.InventoryManagement;
import org.bukkit.Nameable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class BukkitMCVirtualInventoryHolder implements MCVirtualInventoryHolder {

	private VirtualHolder vholder;

	public BukkitMCVirtualInventoryHolder(String id, String title) {
		this.vholder = new VirtualHolder(id, title);
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

	public class VirtualHolder implements InventoryHolder, Nameable {
		private final String id;
		private final String title;

		VirtualHolder(String id, String title) {
			this.id = id;
			this.title = title;
		}

		@Override
		public Inventory getInventory() {
			return (Inventory) InventoryManagement.VIRTUAL_INVENTORIES.get(this.id).getHandle();
		}

		@Override
		public String getCustomName() {
			return title;
		}

		@Override
		public void setCustomName(String name) {
			// not modifiable at this time
		}
	}
}
