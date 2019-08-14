package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCMaterial;

import java.util.Map;

public interface MCShapedRecipe extends MCRecipe {
	Map<Character, MCMaterial[]> getIngredientMap();
	String[] getShape();
	void setIngredient(char key, MCItemStack ingredient);
	void setIngredient(char key, MCMaterial ingredient);
	void setIngredient(char key, MCMaterial... ingredients);
	void setShape(String[] shape);
}
