package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCShapedRecipe;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class BukkitMCShapedRecipe extends BukkitMCRecipe implements MCShapedRecipe {

	ShapedRecipe r;

	public BukkitMCShapedRecipe(ShapedRecipe recipe) {
		super(recipe);
		r = recipe;
	}

	@Override
	public String getKey() {
		return r.getKey().getKey();
	}

	@Override
	public MCRecipeType getRecipeType() {
		return MCRecipeType.SHAPED;
	}

	@Override
	public Object getHandle() {
		return r;
	}

	@Override
	public Map<Character, MCItemStack> getIngredientMap() {
		Map<Character, MCItemStack> ret = new HashMap<>();
		for(Map.Entry<Character, ItemStack> e : r.getIngredientMap().entrySet()) {
			ret.put(e.getKey(), new BukkitMCItemStack(e.getValue()));
		}
		return ret;
	}

	@Override
	public MCItemStack getResult() {
		return new BukkitMCItemStack(r.getResult());
	}

	@Override
	public String[] getShape() {
		return r.getShape();
	}

	@Override
	public void setIngredient(char key, MCItemStack ingredient) {
		r.setIngredient(key, ((ItemStack) ingredient.getHandle()).getData());
	}

	@Override
	public void setShape(String[] shape) {
		r.shape(shape);
	}
}
