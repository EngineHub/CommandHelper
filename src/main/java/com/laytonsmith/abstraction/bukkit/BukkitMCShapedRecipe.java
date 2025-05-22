package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCRecipeChoice;
import com.laytonsmith.abstraction.MCRecipeChoice.ExactChoice;
import com.laytonsmith.abstraction.MCRecipeChoice.MaterialChoice;
import com.laytonsmith.abstraction.MCShapedRecipe;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCMaterial;
import com.laytonsmith.abstraction.enums.MCRecipeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

public class BukkitMCShapedRecipe extends BukkitMCRecipe implements MCShapedRecipe {

	private final ShapedRecipe recipe;

	public BukkitMCShapedRecipe(ShapedRecipe recipe) {
		super(recipe);
		this.recipe = recipe;
	}

	@Override
	public String getKey() {
		return recipe.getKey().getKey();
	}

	@Override
	public MCRecipeType getRecipeType() {
		return MCRecipeType.SHAPED;
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
	public Map<Character, MCRecipeChoice> getIngredientMap() {
		Map<Character, MCRecipeChoice> ret = new HashMap<>();
		for(Map.Entry<Character, RecipeChoice> entry : recipe.getChoiceMap().entrySet()) {
			if(entry.getValue() == null) {
				ret.put(entry.getKey(), null);
			} else if(entry.getValue() instanceof RecipeChoice.MaterialChoice materialChoice) {
				MCRecipeChoice.MaterialChoice choice = new MCRecipeChoice.MaterialChoice();
				for(Material material : materialChoice.getChoices()) {
					choice.addMaterial(BukkitMCMaterial.valueOfConcrete(material));
				}
				ret.put(entry.getKey(), choice);
			} else if(entry.getValue() instanceof RecipeChoice.ExactChoice exactChoice) {
				MCRecipeChoice.ExactChoice choice = new MCRecipeChoice.ExactChoice();
				for(ItemStack itemStack : exactChoice.getChoices()) {
					choice.addItem(new BukkitMCItemStack(itemStack));
				}
				ret.put(entry.getKey(), choice);
			}
		}
		return ret;
	}

	@Override
	public MCItemStack getResult() {
		return new BukkitMCItemStack(recipe.getResult());
	}

	@Override
	public String[] getShape() {
		return recipe.getShape();
	}

	@Override
	public void setIngredient(char key, MCItemStack ingredient) {
		recipe.setIngredient(key, new RecipeChoice.ExactChoice((ItemStack) ingredient.getHandle()));
	}

	@Override
	public void setIngredient(char key, MCRecipeChoice ingredients) {
		if(ingredients instanceof MCRecipeChoice.ExactChoice) {
			List<ItemStack> choice = new ArrayList<>();
			for(MCItemStack itemStack : ((ExactChoice) ingredients).getItems()) {
				choice.add((ItemStack) itemStack.getHandle());
			}
			recipe.setIngredient(key, new RecipeChoice.ExactChoice(choice));
		} else if(ingredients instanceof MCRecipeChoice.MaterialChoice) {
			List<Material> choice = new ArrayList<>();
			for(MCMaterial material : ((MaterialChoice) ingredients).getMaterials()) {
				choice.add((Material) material.getHandle());
			}
			recipe.setIngredient(key, new RecipeChoice.MaterialChoice(choice));
		}
	}

	@Override
	public void setIngredient(char key, MCMaterial mat) {
		recipe.setIngredient(key, (Material) mat.getHandle());
	}

	@Override
	public void setShape(String[] shape) {
		recipe.shape(shape);
	}
}
