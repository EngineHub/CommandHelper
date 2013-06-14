package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryView;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.enums.MCInventoryType;
import org.bukkit.inventory.InventoryView;

/**
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

	public MCInventory getBottomInventory() {
		return new BukkitMCInventory(iv.getBottomInventory());
	}

	public MCInventory getTopInventory() {
		return new BukkitMCInventory(iv.getTopInventory());
	}

	public void close() {
		iv.close();
	}

	public int countSlots() {
		return iv.countSlots();
	}

	public int convertSlot(int rawSlot) {
		return iv.convertSlot(rawSlot);
	}

	public MCItemStack getItem(int slot) {
		return new BukkitMCItemStack(iv.getItem(slot));
	}

	public MCHumanEntity getPlayer() {
		return new BukkitMCHumanEntity(iv.getPlayer());
	}

	public String getTitle() {
		return iv.getTitle();
	}
	
	public MCInventoryType getType() {
		return MCInventoryType.valueOf(this.iv.getType().name());
	}
	
	public void setCursor(MCItemStack item) {
		iv.setCursor(((BukkitMCItemStack)item).__ItemStack());
	}

	public void setItem(int slot, MCItemStack item) {
		iv.setItem(slot, (((BukkitMCItemStack)item).__ItemStack()));
	}
	
}
