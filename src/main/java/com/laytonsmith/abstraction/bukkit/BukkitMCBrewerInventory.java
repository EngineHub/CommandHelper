package com.laytonsmith.abstraction.bukkit;

import org.bukkit.block.BrewingStand;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;

import com.laytonsmith.abstraction.MCBrewerInventory;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.blocks.MCBrewingStand;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBrewingStand;

public class BukkitMCBrewerInventory extends BukkitMCInventory implements MCBrewerInventory {

	private BrewerInventory inv;

	public BukkitMCBrewerInventory(BrewerInventory inv) {
		super(inv);
		this.inv = inv;
	}

	@Override
	public MCItemStack getFuel() {
		return new BukkitMCItemStack(this.inv.getFuel());
	}

	@Override
	public MCItemStack getIngredient() {
		return new BukkitMCItemStack(this.inv.getIngredient());
	}

	@Override
	public MCItemStack getLeftBottle() {
		return new BukkitMCItemStack(this.inv.getItem(0));
	}

	@Override
	public MCItemStack getMiddleBottle() {
		return new BukkitMCItemStack(this.inv.getItem(1));
	}

	@Override
	public MCItemStack getRightBottle() {
		return new BukkitMCItemStack(this.inv.getItem(2));
	}

	@Override
	public void setFuel(MCItemStack stack) {
		this.inv.setFuel((ItemStack) stack.getHandle());
	}

	@Override
	public void setIngredient(MCItemStack stack) {
		this.inv.setIngredient((ItemStack) stack.getHandle());
	}

	@Override
	public void setLeftBottle(MCItemStack stack) {
		this.inv.setItem(0, (ItemStack) stack.getHandle());
	}

	@Override
	public void setMiddleBottle(MCItemStack stack) {
		this.inv.setItem(1, (ItemStack) stack.getHandle());
	}

	@Override
	public void setRightBottle(MCItemStack stack) {
		this.inv.setItem(2, (ItemStack) stack.getHandle());
	}

	@Override
	public MCBrewingStand getHolder() {
		return new BukkitMCBrewingStand((BrewingStand) this.inv.getHolder());
	}
}
