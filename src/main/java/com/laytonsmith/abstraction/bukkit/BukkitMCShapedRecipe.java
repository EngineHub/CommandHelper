package com.laytonsmith.abstraction.bukkit;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCMaterialData;
import com.laytonsmith.abstraction.MCShapedRecipe;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.enums.MCRecipeType;

public class BukkitMCShapedRecipe extends BukkitMCRecipe implements MCShapedRecipe {

	ShapedRecipe r;
	public BukkitMCShapedRecipe(ShapedRecipe recipe) {
		super(recipe);
		r = recipe;
	}
	
	public BukkitMCShapedRecipe(MCItemStack result) {
		this(new ShapedRecipe(((BukkitMCItemStack) result).asItemStack()));
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
		Map<Character, MCItemStack> ret = new HashMap<Character, MCItemStack>();
		for (Map.Entry<Character, ItemStack> e : r.getIngredientMap().entrySet()) {
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
	public MCShapedRecipe setIngredient(char key, MCItemStack ingredient) {
//		int type = ingredient.getTypeId();
//		int data = 0;
//		if (type < 256) {
//			data = ingredient.getData() != null ? ingredient.getData().getData() : 0;
//		} else {
//			data = ingredient.getDurability();
//		}
		MCMaterialData data = null;
		if (ingredient.getTypeId() != 0) {
			data = ingredient.getData();
		}
		return this.setIngredient(key, data);
	}

	@Override
	public MCShapedRecipe setIngredient(char key, int type, int data) {
		MCMaterialData md = null;
		if (type != 0) {
			md = StaticLayer.GetItemStack(type, data, 1).getData();
		}
		return this.setIngredient(key, md);
	}
	
	@Override
	public MCShapedRecipe setIngredient(char key, MCMaterialData data) {
		if (data != null) {
			r.setIngredient(key, ((BukkitMCMaterialData)data).md);
		}
		return this;
	}

	@Override
	public MCShapedRecipe setShape(String[] shape) {
		r.shape(shape);
		return this;
	}
}
