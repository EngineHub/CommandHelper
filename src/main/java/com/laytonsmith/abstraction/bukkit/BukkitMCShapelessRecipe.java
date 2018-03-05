package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCShapelessRecipe;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

public class BukkitMCShapelessRecipe extends BukkitMCRecipe implements MCShapelessRecipe {

	ShapelessRecipe r;

	public BukkitMCShapelessRecipe(ShapelessRecipe recipe) {
		super(recipe);
		r = recipe;
	}

	@Override
	public String getKey() {
		try {
			return r.getKey().getKey();
		} catch(NoSuchMethodError ex) {
			// Probably prior to 1.12
			return null;
		}
	}

	@Override
	public MCRecipeType getRecipeType() {
		return MCRecipeType.SHAPELESS;
	}

	@Override
	public Object getHandle() {
		return r;
	}

	@Override
	public MCItemStack getResult() {
		return new BukkitMCItemStack(r.getResult());
	}

	@Override
	public List<MCItemStack> getIngredients() {
		List<MCItemStack> ret = new ArrayList<>();
		for(ItemStack is : r.getIngredientList()) {
			ret.add(new BukkitMCItemStack(is));
		}
		return ret;
	}

	@Override
	public MCShapelessRecipe addIngredient(MCItemStack ingredient) {
		r.addIngredient(ingredient.getAmount(), ((ItemStack) ingredient.getHandle()).getData());
		return this;
	}
}
