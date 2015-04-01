package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryView;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCHumanEntity;
import com.laytonsmith.abstraction.enums.MCInventoryType;
import org.bukkit.inventory.InventoryView;

/**
 *
 * 
 */
public class BukkitMCInventoryView implements MCInventoryView {
	
	InventoryView iv;

	public BukkitMCInventoryView(InventoryView iv) {
		this.iv = iv;
	}
	
	@Override
	public String toString() {
		return iv.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BukkitMCInventoryView?iv.equals(((BukkitMCInventoryView)obj).iv):false);
	}

	@Override
	public int hashCode() {
		return iv.hashCode();
	}

	@Override
	public MCInventory getBottomInventory() {
		return new BukkitMCInventory(iv.getBottomInventory());
	}

	@Override
	public MCInventory getTopInventory() {
		return new BukkitMCInventory(iv.getTopInventory());
	}

	@Override
	public void close() {
		iv.close();
	}

	@Override
	public int countSlots() {
		return iv.countSlots();
	}

	@Override
	public int convertSlot(int rawSlot) {
		return iv.convertSlot(rawSlot);
	}

	@Override
	public MCItemStack getItem(int slot) {
		return new BukkitMCItemStack(iv.getItem(slot));
	}

	@Override
	public MCHumanEntity getPlayer() {
		return new BukkitMCHumanEntity(iv.getPlayer());
	}

	@Override
	public String getTitle() {
		return iv.getTitle();
	}
	
	@Override
	public MCInventoryType getType() {
		return MCInventoryType.valueOf(this.iv.getType().name());
	}
	
	@Override
	public void setCursor(MCItemStack item) {
		iv.setCursor(((BukkitMCItemStack)item).__ItemStack());
	}

	@Override
	public void setItem(int slot, MCItemStack item) {
		iv.setItem(slot, (((BukkitMCItemStack)item).__ItemStack()));
	}
	
}
