package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCMaterial;

import java.util.List;

public interface MCShapelessRecipe extends MCRecipe {
	void addIngredient(MCItemStack ingredient);
	void addIngredient(MCMaterial ingredient);
	void addIngredient(MCMaterial... ingredients);
	List<MCMaterial[]> getIngredients();
}
