package com.laytonsmith.abstraction;

import java.util.List;

public interface MCShapelessRecipe extends MCRecipe {

	String getKey();

	MCShapelessRecipe addIngredient(MCItemStack ingredient);

	List<MCItemStack> getIngredients();
}
