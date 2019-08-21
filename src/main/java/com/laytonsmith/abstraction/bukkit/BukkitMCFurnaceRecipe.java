package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCCookingRecipe;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCMaterial;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.util.List;

public class BukkitMCFurnaceRecipe extends BukkitMCRecipe implements MCCookingRecipe {

	private FurnaceRecipe recipe;

	public BukkitMCFurnaceRecipe(FurnaceRecipe recipe) {
		super(recipe);
		this.recipe = recipe;
	}

	@Override
	public String getKey() {
		return recipe.getKey().getKey();
	}

	@Override
	public MCRecipeType getRecipeType() {
		return MCRecipeType.FURNACE;
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

	@Override
	public int getCookingTime() {
		return recipe.getCookingTime();
	}

	@Override
	public void setCookingTime(int ticks) {
		recipe.setCookingTime(ticks);
	}

	@Override
	public float getExperience() {
		return recipe.getExperience();
	}

	@Override
	public void setExperience(float exp) {
		recipe.setExperience(exp);
	}
}
