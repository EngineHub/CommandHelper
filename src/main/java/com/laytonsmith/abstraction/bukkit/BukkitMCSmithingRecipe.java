package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCSmithingRecipe;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCMaterial;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import org.bukkit.Material;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingRecipe;

import java.util.List;

public class BukkitMCSmithingRecipe extends BukkitMCRecipe implements MCSmithingRecipe {

	private final SmithingRecipe recipe;

	public BukkitMCSmithingRecipe(SmithingRecipe recipe) {
		super(recipe);
		this.recipe = recipe;
	}

	@Override
	public String getKey() {
		return recipe.getKey().getKey();
	}

	@Override
	public MCRecipeType getRecipeType() {
		return MCRecipeType.SMITHING;
	}

	@Override
	public String getGroup() {
		return "";
	}

	@Override
	public void setGroup(String group) {}

	@Override
	public MCItemStack getResult() {
		return new BukkitMCItemStack(recipe.getResult());
	}

	@Override
	public Object getHandle() {
		return recipe;
	}

	@Override
	public MCMaterial[] getBase() {
		List<Material> choices = ((RecipeChoice.MaterialChoice) recipe.getBase()).getChoices();
		MCMaterial[] ret = new MCMaterial[choices.size()];
		for(int i = 0; i < choices.size(); i++) {
			ret[i] = new BukkitMCMaterial(choices.get(i));
		}
		return ret;
	}

	@Override
	public MCMaterial[] getAddition() {
		List<Material> choices = ((RecipeChoice.MaterialChoice) recipe.getAddition()).getChoices();
		MCMaterial[] ret = new MCMaterial[choices.size()];
		for(int i = 0; i < choices.size(); i++) {
			ret[i] = new BukkitMCMaterial(choices.get(i));
		}
		return ret;
	}
}
