package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCItemStack;
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

	private ShapelessRecipe recipe;

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
	public List<MCMaterial[]> getIngredients() {
		List<RecipeChoice> choiceList = recipe.getChoiceList();
		List<MCMaterial[]> ret = new ArrayList<>(choiceList.size());
		for(RecipeChoice choice : choiceList) {
			List<Material> choices = ((RecipeChoice.MaterialChoice) choice).getChoices();
			MCMaterial[] list = new MCMaterial[choices.size()];
			for(int i = 0; i < choices.size(); i++) {
				list[i] = new BukkitMCMaterial(choices.get(i));
			}
			ret.add(list);
		}
		return ret;
	}

	@Override
	public void addIngredient(MCItemStack ingredient) {
		recipe.addIngredient(ingredient.getAmount(), ((ItemStack) ingredient.getHandle()).getType());
	}

	@Override
	public void addIngredient(MCMaterial ingredient) {
		recipe.addIngredient((Material) ingredient.getHandle());
	}

	@Override
	public void addIngredient(MCMaterial... ingredients) {
		Material[] concrete = new Material[ingredients.length];
		for(int i = 0; i < ingredients.length; i++) {
			concrete[i] = (Material) ingredients[i].getHandle();
		}
		recipe.addIngredient(new RecipeChoice.MaterialChoice(concrete));
	}
}
