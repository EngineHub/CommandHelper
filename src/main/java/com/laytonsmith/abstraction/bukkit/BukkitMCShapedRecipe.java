package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCShapedRecipe;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCMaterial;
import com.laytonsmith.abstraction.enums.MCRecipeType;

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
	public Map<Character, MCMaterial[]> getIngredientMap() {
		Map<Character, MCMaterial[]> ret = new HashMap<>();
		for(Map.Entry<Character, RecipeChoice> e : recipe.getChoiceMap().entrySet()) {
			if(e.getValue() == null) {
				ret.put(e.getKey(), null);
			} else {
				List<Material> choices = ((RecipeChoice.MaterialChoice) e.getValue()).getChoices();
				MCMaterial[] list = new MCMaterial[choices.size()];
				for(int i = 0; i < choices.size(); i++) {
					list[i] = BukkitMCMaterial.valueOfConcrete(choices.get(i));
				}
				ret.put(e.getKey(), list);
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
	public void setIngredient(char key, MCItemStack... ingredients) {
		ItemStack[] concrete = new ItemStack[ingredients.length];
		for(int i = 0; i < ingredients.length; i++) {
			concrete[i] = (ItemStack) ingredients[i].getHandle();
		}
		recipe.setIngredient(key, new RecipeChoice.ExactChoice(concrete));
	}

	@Override
	public void setIngredient(char key, MCMaterial mat) {
		recipe.setIngredient(key, (Material) mat.getHandle());
	}

	@Override
	public void setIngredient(char key, MCMaterial... ingredients) {
		Material[] concrete = new Material[ingredients.length];
		for(int i = 0; i < ingredients.length; i++) {
			concrete[i] = (Material) ingredients[i].getHandle();
		}
		recipe.setIngredient(key, new RecipeChoice.MaterialChoice(concrete));
	}

	@Override
	public void setShape(String[] shape) {
		recipe.shape(shape);
	}
}
