package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCFurnaceInventory;
import com.laytonsmith.abstraction.blocks.MCFurnace;
import com.laytonsmith.abstraction.bukkit.BukkitMCFurnaceInventory;

import org.bukkit.block.Furnace;

public class BukkitMCFurnace extends BukkitMCBlockState implements MCFurnace {
	
	private Furnace furnace;
	
	public BukkitMCFurnace(Furnace block) {
		super(block);
		this.furnace = block;
	}
	
	@Override
	public MCFurnaceInventory getInventory() {
		return new BukkitMCFurnaceInventory(this.furnace.getInventory());
	}
	
	@Override
	public short getBurnTime() {
		return this.furnace.getBurnTime();
	}
	
	@Override
	public void setBurnTime(short burnTime) {
		this.furnace.setBurnTime(burnTime);
	}
	
	@Override
	public short getCookTime() {
		return this.furnace.getCookTime();
	}
	
	@Override
	public void setCookTime(short cookTime) {
		this.furnace.setCookTime(cookTime);
	}
}
