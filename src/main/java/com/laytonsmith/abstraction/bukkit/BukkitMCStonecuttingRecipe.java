package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCRecipeChoice;
import com.laytonsmith.abstraction.MCRecipeChoice.ExactChoice;
import com.laytonsmith.abstraction.MCRecipeChoice.MaterialChoice;
import com.laytonsmith.abstraction.MCStonecuttingRecipe;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCMaterial;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.StonecuttingRecipe;

import java.util.ArrayList;
import java.util.List;

public class BukkitMCStonecuttingRecipe extends BukkitMCRecipe implements MCStonecuttingRecipe {

	private final StonecuttingRecipe recipe;

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
	public MCRecipeChoice getInput() {
		RecipeChoice recipeChoice = recipe.getInputChoice();
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
		recipe.setInputChoice(new RecipeChoice.ExactChoice((ItemStack) input.getHandle()));
	}

	@Override
	public void setInput(MCMaterial mat) {
		recipe.setInput((Material) mat.getHandle());
	}

	@Override
	public void setInput(MCRecipeChoice choice) {
		if(choice instanceof MCRecipeChoice.ExactChoice) {
			List<ItemStack> itemChoice = new ArrayList<>();
			for(MCItemStack itemStack : ((ExactChoice) choice).getItems()) {
				itemChoice.add((ItemStack) itemStack.getHandle());
			}
			recipe.setInputChoice(new RecipeChoice.ExactChoice(itemChoice));
		} else if(choice instanceof MCRecipeChoice.MaterialChoice) {
			List<Material> materialChoice = new ArrayList<>();
			for(MCMaterial material : ((MaterialChoice) choice).getMaterials()) {
				materialChoice.add((Material) material.getHandle());
			}
			recipe.setInputChoice(new RecipeChoice.MaterialChoice(materialChoice));
		}
	}
}
