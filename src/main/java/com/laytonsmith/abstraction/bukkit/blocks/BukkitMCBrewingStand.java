package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCBrewerInventory;
import com.laytonsmith.abstraction.blocks.MCBrewingStand;
import com.laytonsmith.abstraction.bukkit.BukkitMCBrewerInventory;

import org.bukkit.block.BrewingStand;

public class BukkitMCBrewingStand extends BukkitMCBlockState implements MCBrewingStand {

	private BrewingStand bs;

	public BukkitMCBrewingStand(BrewingStand block) {
		super(block);
		this.bs = block;
	}

	@Override
	public MCBrewerInventory getInventory() {
		return new BukkitMCBrewerInventory(this.bs.getInventory());
	}

	@Override
	public int getBrewingTime() {
		return this.bs.getBrewingTime();
	}

	@Override
	public int getFuelLevel() {
		return this.bs.getFuelLevel();
	}

	@Override
	public void setBrewingTime(int brewTime) {
		this.bs.setBrewingTime(brewTime);
	}

	@Override
	public void setFuelLevel(int level) {
		this.bs.setFuelLevel(level);
	}
}
