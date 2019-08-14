package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCCookingRecipe;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCMaterial;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import org.bukkit.Material;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.util.List;

public class BukkitMCCookingRecipe extends BukkitMCRecipe implements MCCookingRecipe {

	private MCRecipeType type;

	public BukkitMCCookingRecipe(Recipe recipe, MCRecipeType type) {
		super(recipe);
		this.type = type;
	}

	@Override
	public String getKey() {
		return getHandle().getKey().getKey();
	}

	@Override
	public MCRecipeType getRecipeType() {
		return type;
	}

	@Override
	public String getGroup() {
		return getHandle().getGroup();
	}

	@Override
	public void setGroup(String group) {
		getHandle().setGroup(group);
	}

	@Override
	public MCItemStack getResult() {
		return new BukkitMCItemStack(getHandle().getResult());
	}

	@Override
	public CookingRecipe getHandle() {
		return (CookingRecipe) super.getHandle();
	}

	@Override
	public MCMaterial[] getInput() {
		List<Material> choices = ((RecipeChoice.MaterialChoice) getHandle().getInputChoice()).getChoices();
		MCMaterial[] ret = new MCMaterial[choices.size()];
		for(int i = 0; i < choices.size(); i++) {
			ret[i] = new BukkitMCMaterial(choices.get(i));
		}
		return ret;
	}

	@Override
	public void setInput(MCItemStack input) {
		getHandle().setInput(((ItemStack) input.getHandle()).getType());
	}

	@Override
	public void setInput(MCMaterial mat) {
		getHandle().setInput((Material) mat.getHandle());
	}

	@Override
	public void setInput(MCMaterial... mats) {
		Material[] concrete = new Material[mats.length];
		for(int i = 0; i < mats.length; i++) {
			concrete[i] = (Material) mats[i].getHandle();
		}
		getHandle().setInputChoice(new RecipeChoice.MaterialChoice(concrete));
	}

	@Override
	public int getCookingTime() {
		return getHandle().getCookingTime();
	}

	@Override
	public void setCookingTime(int ticks) {
		getHandle().setCookingTime(ticks);
	}

	@Override
	public float getExperience() {
		return getHandle().getExperience();
	}

	@Override
	public void setExperience(float exp) {
		getHandle().setExperience(exp);
	}
}
