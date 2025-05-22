package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCMaterial;

import java.util.List;

public interface MCShapelessRecipe extends MCRecipe {
	void addIngredient(MCMaterial ingredient, int amount);
	void addIngredient(MCMaterial ingredient);
	void addIngredient(MCRecipeChoice choice);
	List<MCRecipeChoice> getIngredients();
}
