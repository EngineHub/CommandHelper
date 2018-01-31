package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCFurnaceRecipe;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;

public class BukkitMCFurnaceRecipe extends BukkitMCRecipe implements MCFurnaceRecipe {

	FurnaceRecipe fr;
	public BukkitMCFurnaceRecipe(FurnaceRecipe recipe) {
		super(recipe);
		fr = recipe;
	}

	@Override
	public MCRecipeType getRecipeType() {
		return MCRecipeType.FURNACE;
	}

	@Override
	public MCItemStack getResult() {
		return new BukkitMCItemStack(fr.getResult());
	}

	@Override
	public Object getHandle() {
		return fr;
	}

	@Override
	public MCItemStack getInput() {
		return new BukkitMCItemStack(fr.getInput());
	}

	@Override
	public void setInput(MCItemStack input) {
		fr.setInput(((ItemStack) input.getHandle()).getData());
	}
}
