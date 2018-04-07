package com.laytonsmith.abstraction.bukkit;

import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

import com.laytonsmith.abstraction.MCFurnaceInventory;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.blocks.MCFurnace;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCFurnace;

public class BukkitMCFurnaceInventory extends BukkitMCInventory implements MCFurnaceInventory {
	
	private FurnaceInventory inv;
	
	public BukkitMCFurnaceInventory(FurnaceInventory inv) {
		super(inv);
		this.inv = inv;
	}
	
	@Override
	public MCItemStack getResult() {
		return new BukkitMCItemStack(this.inv.getResult());
	}
	
	@Override
	public MCItemStack getFuel() {
		return new BukkitMCItemStack(this.inv.getFuel());
	}
	
	@Override
	public MCItemStack getSmelting() {
		return new BukkitMCItemStack(this.inv.getSmelting());
	}
	
	@Override
	public void setFuel(MCItemStack stack) {
		this.inv.setFuel((ItemStack) stack.getHandle());
	}
	
	@Override
	public void setResult(MCItemStack stack) {
		this.inv.setResult((ItemStack) stack.getHandle());
	}
	
	@Override
	public void setSmelting(MCItemStack stack) {
		this.inv.setSmelting((ItemStack) stack.getHandle());
	}
	
	@Override
	public MCFurnace getHolder() {
		return new BukkitMCFurnace(this.inv.getHolder());
	}
}
