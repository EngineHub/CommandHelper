package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCCookingRecipe;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCRecipeChoice;
import com.laytonsmith.abstraction.MCRecipeChoice.ExactChoice;
import com.laytonsmith.abstraction.MCRecipeChoice.MaterialChoice;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCMaterial;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import org.bukkit.Material;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.util.ArrayList;
import java.util.List;

public class BukkitMCCookingRecipe extends BukkitMCRecipe implements MCCookingRecipe {

	private final MCRecipeType type;

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
	public MCRecipeChoice getInput() {
		RecipeChoice recipeChoice = getHandle().getInputChoice();
		if(recipeChoice instanceof RecipeChoice.MaterialChoice materialChoice) {
			MCRecipeChoice.MaterialChoice choice = new MCRecipeChoice.MaterialChoice();
			for(Material material : materialChoice.getChoices()) {
				choice.addMaterial(BukkitMCMaterial.valueOfConcrete(material));
			}
			return choice;
		} else if(recipeChoice instanceof RecipeChoice.ExactChoice exactChoice) {
			MCRecipeChoice.ExactChoice choice = new MCRecipeChoice.ExactChoice();
			for(ItemStack itemStack : exactChoice.getChoices()) {
				choice.addItem(new BukkitMCItemStack(itemStack));
			}
			return choice;
		}
		throw new UnsupportedOperationException("Unsupported recipe choice");
	}

	@Override
	public void setInput(MCItemStack input) {
		getHandle().setInputChoice(new RecipeChoice.ExactChoice((ItemStack) input.getHandle()));
	}

	@Override
	public void setInput(MCMaterial mat) {
		getHandle().setInput((Material) mat.getHandle());
	}

	@Override
	public void setInput(MCRecipeChoice choice) {
		if(choice instanceof MCRecipeChoice.ExactChoice) {
			List<ItemStack> itemChoice = new ArrayList<>();
			for(MCItemStack itemStack : ((ExactChoice) choice).getItems()) {
				itemChoice.add((ItemStack) itemStack.getHandle());
			}
			getHandle().setInputChoice(new RecipeChoice.ExactChoice(itemChoice));
		} else if(choice instanceof MCRecipeChoice.MaterialChoice) {
			List<Material> materialChoice = new ArrayList<>();
			for(MCMaterial material : ((MaterialChoice) choice).getMaterials()) {
				materialChoice.add((Material) material.getHandle());
			}
			getHandle().setInputChoice(new RecipeChoice.MaterialChoice(materialChoice));
		}
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
