package com.laytonsmith.abstraction.bukkit;

import org.bukkit.inventory.Recipe;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCRecipe;
import com.laytonsmith.abstraction.enums.MCRecipeType;

public abstract class BukkitMCRecipe implements MCRecipe {

	Recipe r;
	protected BukkitMCRecipe(Recipe rec) {
		r = rec;
	}
	
	@Override
	public Object getHandle() {
		return r;
	}
	
	@Override
	public MCItemStack getResult() {
		return new BukkitMCItemStack(r.getResult());
	}
	
	public abstract MCRecipeType getRecipeType();
}
