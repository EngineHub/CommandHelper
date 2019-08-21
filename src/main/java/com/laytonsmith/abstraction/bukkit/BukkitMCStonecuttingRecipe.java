package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCStonecuttingRecipe;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCMaterial;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.StonecuttingRecipe;

import java.util.List;

public class BukkitMCStonecuttingRecipe extends BukkitMCRecipe implements MCStonecuttingRecipe {

	private StonecuttingRecipe recipe;

	public BukkitMCStonecuttingRecipe(StonecuttingRecipe recipe) {
		super(recipe);
		this.recipe = recipe;
	}

	@Override
	public String getKey() {
		return recipe.getKey().getKey();
	}

	@Override
	public MCRecipeType getRecipeType() {
		return MCRecipeType.STONECUTTING;
	}

	@Override
	public String getGroup() {
		return recipe.getGroup();
	}

	@Override
	public void setGroup(String group) {
		recipe.setGroup(group);
	}

	@Override
	public MCItemStack getResult() {
		return new BukkitMCItemStack(recipe.getResult());
	}

	@Override
	public Object getHandle() {
		return recipe;
	}

	@Override
	public MCMaterial[] getInput() {
		List<Material> choices = ((RecipeChoice.MaterialChoice) recipe.getInputChoice()).getChoices();
		MCMaterial[] ret = new MCMaterial[choices.size()];
		for(int i = 0; i < choices.size(); i++) {
			ret[i] = new BukkitMCMaterial(choices.get(i));
		}
		return ret;
	}

	@Override
	public void setInput(MCItemStack input) {
		recipe.setInput(((ItemStack) input.getHandle()).getType());
	}

	@Override
	public void setInput(MCMaterial mat) {
		recipe.setInput((Material) mat.getHandle());
	}

	@Override
	public void setInput(MCMaterial... mats) {
		Material[] concrete = new Material[mats.length];
		for(int i = 0; i < mats.length; i++) {
			concrete[i] = (Material) mats[i].getHandle();
		}
		recipe.setInputChoice(new RecipeChoice.MaterialChoice(concrete));
	}
}
