package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCRecipeChoice;
import com.laytonsmith.abstraction.MCRecipeChoice.ExactChoice;
import com.laytonsmith.abstraction.MCRecipeChoice.MaterialChoice;
import com.laytonsmith.abstraction.MCBrewingRecipe;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCMaterial;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.BrewingRecipe;

public class BukkitMCBrewingRecipe extends BukkitMCRecipe implements MCBrewingRecipe {

	private final BrewingRecipe recipe;

	public BukkitMCBrewingRecipe(BrewingRecipe recipe) {
		super(recipe);
		this.recipe = recipe;
	}

	@Override
	public String getKey() {
		return recipe.getKey().getKey();
	}

	@Override
	public MCRecipeType getRecipeType() {
		return MCRecipeType.BREWING;
	}

	@Override
	public String getGroup() {
		return "";
	}

	@Override
	public void setGroup(String group) {
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
	public MCRecipeChoice getInput() {
		RecipeChoice recipeChoice = recipe.getInput();
		if(recipeChoice instanceof RecipeChoice.MaterialChoice materialChoice) {
			MaterialChoice choice = new MaterialChoice();
			for(Material material : materialChoice.getChoices()) {
				choice.addMaterial(BukkitMCMaterial.valueOfConcrete(material));
			}
			return choice;
		} else if(recipeChoice instanceof RecipeChoice.ExactChoice exactChoice) {
			ExactChoice choice = new ExactChoice();
			for(ItemStack itemStack : exactChoice.getChoices()) {
				choice.addItem(new BukkitMCItemStack(itemStack));
			}
			return choice;
		}
		throw new UnsupportedOperationException("Unsupported recipe choice");
	}

	@Override
	public MCRecipeChoice getIngredient() {
		RecipeChoice recipeChoice = recipe.getIngredient();
		if(recipeChoice instanceof RecipeChoice.MaterialChoice materialChoice) {
			MaterialChoice choice = new MaterialChoice();
			for(Material material : materialChoice.getChoices()) {
				choice.addMaterial(BukkitMCMaterial.valueOfConcrete(material));
			}
			return choice;
		} else if(recipeChoice instanceof RecipeChoice.ExactChoice exactChoice) {
			ExactChoice choice = new ExactChoice();
			for(ItemStack itemStack : exactChoice.getChoices()) {
				choice.addItem(new BukkitMCItemStack(itemStack));
			}
			return choice;
		}
		throw new UnsupportedOperationException("Unsupported recipe choice");
	}
}
