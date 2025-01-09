package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCGrindstoneInventory;
import com.laytonsmith.abstraction.MCItemStack;
import org.bukkit.inventory.GrindstoneInventory;

public class BukkitMCGrindstoneInventory extends BukkitMCInventory implements MCGrindstoneInventory {

	GrindstoneInventory gi;

	public BukkitMCGrindstoneInventory(GrindstoneInventory inventory) {
		super(inventory);
		gi = inventory;
	}

	@Override
	public MCItemStack getUpperItem() {
		return new BukkitMCItemStack(gi.getItem(0));
	}

	@Override
	public MCItemStack getLowerItem() {
		return new BukkitMCItemStack(gi.getItem(1));
	}

	@Override
	public MCItemStack getResult() {
		return new BukkitMCItemStack(gi.getItem(2));
	}

	@Override
	public void setUpperItem(MCItemStack stack) {
		gi.setItem(0, ((BukkitMCItemStack) stack).asItemStack());
	}

	@Override
	public void setLowerItem(MCItemStack stack) {
		gi.setItem(1, ((BukkitMCItemStack) stack).asItemStack());
	}

	@Override
	public void setResult(MCItemStack stack) {
		gi.setItem(2, ((BukkitMCItemStack) stack).asItemStack());
	}
}
