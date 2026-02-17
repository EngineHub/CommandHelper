package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCMaterial;

import java.util.Map;

public interface MCShapedRecipe extends MCRecipe {
	Map<Character, MCRecipeChoice> getIngredientMap();
	String[] getShape();
	void setIngredient(char key, MCItemStack ingredient);
	void setIngredient(char key, MCRecipeChoice ingredient);
	void setIngredient(char key, MCMaterial ingredient);
	void setShape(String[] shape);
}
