package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryView;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventoryView;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.enums.MCGameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BukkitMCHumanEntity extends BukkitMCLivingEntity implements MCHumanEntity {

	HumanEntity he;

	public BukkitMCHumanEntity(Entity humanEntity) {
		super(humanEntity);
		he = (HumanEntity) humanEntity;
	}

	public HumanEntity asHumanEntity() {
		return he;
	}

	@Override
	public String getName() {
		return he.getName();
	}

	@Override
	public void closeInventory() {
		he.closeInventory();
	}

	@Override
	public MCGameMode getGameMode() {
		return MCGameMode.valueOf(he.getGameMode().name());
	}

	@Override
	public MCItemStack getItemInHand() {
		// deprecated in 1.9
		ItemStack is = he.getItemInHand();
		return is == null ? null : new BukkitMCItemStack(is);
	}

	@Override
	public MCItemStack getItemOnCursor() {
		return new BukkitMCItemStack(he.getItemOnCursor());
	}

	@Override
	public int getSleepTicks() {
		return he.getSleepTicks();
	}

	@Override
	public boolean isBlocking() {
		return he.isBlocking();
	}

	@Override
	public boolean isSleeping() {
		return he.isSleeping();
	}

	@Override
	public void setGameMode(MCGameMode mode) {
		he.setGameMode(org.bukkit.GameMode.valueOf(mode.name()));
	}

	@Override
	public void setItemInHand(MCItemStack item) {
		// deprecated in 1.9
		he.setItemInHand(((BukkitMCItemStack) item).asItemStack());
	}

	@Override
	public void setItemOnCursor(MCItemStack item) {
		he.setItemOnCursor(((BukkitMCItemStack) item).asItemStack());
	}

	@Override
	public int getCooldown(MCMaterial material) {
		try {
			return he.getCooldown((Material) material.getHandle());
		} catch (NoSuchMethodError ex) {
			// Probably prior to 1.11.2
			return 0;
		}
	}

	@Override
	public void setCooldown(MCMaterial material, int ticks) {
		try {
			he.setCooldown((Material) material.getHandle(), ticks);
		} catch (NoSuchMethodError ex) {
			// Probably prior to 1.11.2
		}
	}

	@Override
	public MCInventoryView openInventory(MCInventory inventory) {
		return new BukkitMCInventoryView(he.openInventory((Inventory) inventory.getHandle()));
	}

	@Override
	public MCInventoryView getOpenInventory() {
		return new BukkitMCInventoryView(he.getOpenInventory());
	}

	@Override
	public MCInventory getInventory() {
		return new BukkitMCInventory(he.getInventory());
	}

	@Override
	public MCInventory getEnderChest() {
		return new BukkitMCInventory(he.getEnderChest());
	}

	@Override
	public MCInventoryView openWorkbench(MCLocation loc, boolean force) {
		return new BukkitMCInventoryView(he.openWorkbench((Location) loc.getHandle(), force));
	}

	@Override
	public MCInventoryView openEnchanting(MCLocation loc, boolean force) {
		return new BukkitMCInventoryView(he.openEnchanting((Location) loc.getHandle(), force));
	}
}
