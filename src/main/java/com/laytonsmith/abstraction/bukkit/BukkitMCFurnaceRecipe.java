package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCFurnaceRecipe;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;

public class BukkitMCFurnaceRecipe extends BukkitMCRecipe implements MCFurnaceRecipe {

	FurnaceRecipe fr;

	public BukkitMCFurnaceRecipe(FurnaceRecipe recipe) {
		super(recipe);
		fr = recipe;
	}

	@Override
	public String getKey() {
		return fr.getKey().getKey();
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
		fr.setInput(((ItemStack) input.getHandle()).getType());
	}

	@Override
	public void setInput(MCMaterial mat) {
		fr.setInput((Material) mat.getHandle());
	}
}
