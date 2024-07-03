package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryView;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCHumanEntity;
import com.laytonsmith.abstraction.enums.MCInventoryType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

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
		return obj instanceof BukkitMCInventoryView && iv.equals(((BukkitMCInventoryView) obj).iv);
	}

	@Override
	public int hashCode() {
		return iv.hashCode();
	}

	@Override
	public MCInventory getBottomInventory() {
		return new BukkitMCInventory(ReflectionUtils.invokeMethod(iv, "getBottomInventory"));
	}

	@Override
	public MCInventory getTopInventory() {
		return new BukkitMCInventory(ReflectionUtils.invokeMethod(iv, "getTopInventory"));
	}

	@Override
	public void close() {
		ReflectionUtils.invokeMethod(iv, "close");
	}

	@Override
	public int countSlots() {
		return ReflectionUtils.invokeMethod(iv, "countSlots");
	}

	@Override
	public int convertSlot(int rawSlot) {
		return ReflectionUtils.invokeMethod(iv, "convertSlot", rawSlot);
	}

	@Override
	public MCItemStack getItem(int slot) {
		return new BukkitMCItemStack((ItemStack) ReflectionUtils.invokeMethod(iv, "getItem", slot));
	}

	@Override
	public MCHumanEntity getPlayer() {
		return new BukkitMCHumanEntity(ReflectionUtils.invokeMethod(iv, "getPlayer"));
	}

	@Override
	public String getTitle() {
		return ReflectionUtils.invokeMethod(iv, "getTitle");
	}

	@Override
	public MCInventoryType getType() {
		return MCInventoryType.valueOf(((InventoryType) ReflectionUtils.invokeMethod(iv, "getType")).name());
	}

	@Override
	public void setCursor(MCItemStack item) {
		ReflectionUtils.invokeMethod(iv, "setCursor", ((BukkitMCItemStack) item).__ItemStack());
	}

	@Override
	public void setItem(int slot, MCItemStack item) {
		ReflectionUtils.invokeMethod(iv, "setItem", slot, (((BukkitMCItemStack) item).__ItemStack()));
	}
}
