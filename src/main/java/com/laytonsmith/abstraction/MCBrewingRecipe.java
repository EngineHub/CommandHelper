package com.laytonsmith.abstraction;

public interface MCBrewingRecipe extends MCRecipe {
	MCRecipeChoice getInput();
	MCRecipeChoice getIngredient();
}
