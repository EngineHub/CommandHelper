package com.laytonsmith.abstraction.bukkit;

import org.bukkit.block.Beacon;
import org.bukkit.inventory.BeaconInventory;
import org.bukkit.inventory.ItemStack;

import com.laytonsmith.abstraction.MCBeaconInventory;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.blocks.MCBeacon;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBeacon;

public class BukkitMCBeaconInventory extends BukkitMCInventory implements MCBeaconInventory {

	private BeaconInventory inv;

	public BukkitMCBeaconInventory(BeaconInventory inv) {
		super(inv);
		this.inv = inv;
	}

	@Override
	public MCItemStack getItem() {
		return new BukkitMCItemStack(this.inv.getItem());
	}

	@Override
	public void setItem(MCItemStack stack) {
		this.inv.setItem((ItemStack) stack.getHandle());
	}

	@Override
	public MCBeacon getHolder() {
		return new BukkitMCBeacon((Beacon) this.inv.getHolder());
	}
}
