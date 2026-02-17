package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCRecipeChoice;
import com.laytonsmith.abstraction.MCRecipeChoice.ExactChoice;
import com.laytonsmith.abstraction.MCRecipeChoice.MaterialChoice;
import com.laytonsmith.abstraction.MCShapelessRecipe;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCMaterial;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;

public class BukkitMCShapelessRecipe extends BukkitMCRecipe implements MCShapelessRecipe {

	private final ShapelessRecipe recipe;

	public BukkitMCShapelessRecipe(ShapelessRecipe recipe) {
		super(recipe);
		this.recipe = recipe;
	}

	@Override
	public String getKey() {
		return recipe.getKey().getKey();
	}

	@Override
	public MCRecipeType getRecipeType() {
		return MCRecipeType.SHAPELESS;
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
	public Object getHandle() {
		return recipe;
	}

	@Override
	public MCItemStack getResult() {
		return new BukkitMCItemStack(recipe.getResult());
	}

	@Override
	public List<MCRecipeChoice> getIngredients() {
		List<RecipeChoice> choiceList = recipe.getChoiceList();
		List<MCRecipeChoice> ret = new ArrayList<>(choiceList.size());
		for(RecipeChoice recipeChoice : choiceList) {
			if(recipeChoice instanceof RecipeChoice.MaterialChoice materialChoice) {
				MCRecipeChoice.MaterialChoice choice = new MCRecipeChoice.MaterialChoice();
				for(Material material : materialChoice.getChoices()) {
					choice.addMaterial(BukkitMCMaterial.valueOfConcrete(material));
				}
				ret.add(choice);
			} else if(recipeChoice instanceof RecipeChoice.ExactChoice exactChoice) {
				MCRecipeChoice.ExactChoice choice = new MCRecipeChoice.ExactChoice();
				for(ItemStack itemStack : exactChoice.getChoices()) {
					choice.addItem(new BukkitMCItemStack(itemStack));
				}
				ret.add(choice);
			}
		}
		return ret;
	}

	@Override
	public void addIngredient(MCMaterial ingredient, int amount) {
		recipe.addIngredient(amount, (Material) ingredient.getHandle());
	}

	@Override
	public void addIngredient(MCMaterial ingredient) {
		recipe.addIngredient((Material) ingredient.getHandle());
	}

	@Override
	public void addIngredient(MCRecipeChoice choice) {
		if(choice instanceof MCRecipeChoice.ExactChoice) {
			List<ItemStack> itemChoice = new ArrayList<>();
			for(MCItemStack itemStack : ((ExactChoice) choice).getItems()) {
				itemChoice.add((ItemStack) itemStack.getHandle());
			}
			recipe.addIngredient(new RecipeChoice.ExactChoice(itemChoice));
		} else if(choice instanceof MCRecipeChoice.MaterialChoice) {
			List<Material> materialChoice = new ArrayList<>();
			for(MCMaterial material : ((MaterialChoice) choice).getMaterials()) {
				materialChoice.add((Material) material.getHandle());
			}
			recipe.addIngredient(new RecipeChoice.MaterialChoice(materialChoice));
		}
	}
}
