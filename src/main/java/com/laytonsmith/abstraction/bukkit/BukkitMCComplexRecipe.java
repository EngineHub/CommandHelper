package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCComplexRecipe;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import org.bukkit.Keyed;
import org.bukkit.inventory.Recipe;

public class BukkitMCComplexRecipe extends BukkitMCRecipe implements MCComplexRecipe {

	public BukkitMCComplexRecipe(Recipe recipe) {
		super(recipe);
	}

	@Override
	public String getKey() {
		return ((Keyed) getHandle()).getKey().getKey();
	}

	@Override
	public MCRecipeType getRecipeType() {
		return MCRecipeType.COMPLEX;
	}

	@Override
	public String getGroup() {
		return null;
	}

	@Override
	public void setGroup(String group) {
		// complex recipes are basically dummy recipes with no group
	}

	@Override
	public MCItemStack getResult() {
		return new BukkitMCItemStack(((Recipe) getHandle()).getResult());
	}
}
