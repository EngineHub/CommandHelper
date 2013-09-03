package com.laytonsmith.abstraction.bukkit;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCShapelessRecipe;
import com.laytonsmith.abstraction.enums.MCRecipeType;

/**
 * 
 * @author jb_aero
 */
public class BukkitMCShapelessRecipe extends BukkitMCRecipe implements MCShapelessRecipe {

	ShapelessRecipe r;
	public BukkitMCShapelessRecipe(ShapelessRecipe recipe) {
		super(recipe);
		r = recipe;
	}
	
	public BukkitMCShapelessRecipe(MCItemStack result) {
		this(new ShapelessRecipe(((BukkitMCItemStack) result).asItemStack()));
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
		List<MCItemStack> ret = new ArrayList<MCItemStack>();
		for (ItemStack is : r.getIngredientList()) {
			ret.add(new BukkitMCItemStack(is));
		}
		return ret;
	}

	@Override
	public MCShapelessRecipe addIngredient(MCItemStack ingredient) {
		int type = ingredient.getTypeId();
		int data = 0;
		if (type < 256) {
			data = ingredient.getData() != null ? ingredient.getData().getData() : 0;
		} else {
			data = ingredient.getDurability();
		}
		return this.addIngredient(type, data, ingredient.getAmount());
	}

	@Override
	public MCShapelessRecipe addIngredient(int type, int data, int amount) {
		r.addIngredient(amount, Material.getMaterial(type), data);
		return this;
	}

	@Override
	public MCShapelessRecipe removeIngredient(MCItemStack ingredient) {
		int type = ingredient.getTypeId();
		int data = 0;
		if (type < 256) {
			data = ingredient.getData() != null ? ingredient.getData().getData() : 0;
		} else {
			data = ingredient.getDurability();
		}
		return this.removeIngredient(type, data, ingredient.getAmount());
	}

	@Override
	public MCShapelessRecipe removeIngredient(int type, int data, int amount) {
		r.removeIngredient(amount, Material.getMaterial(type), data);
		return this;
	}
}
