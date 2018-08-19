package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCMaterial;

import java.util.List;

public interface MCShapelessRecipe extends MCRecipe {

	String getKey();

	MCShapelessRecipe addIngredient(MCItemStack ingredient);

	MCShapelessRecipe addIngredient(MCMaterial ingredient);

	List<MCItemStack> getIngredients();
}
