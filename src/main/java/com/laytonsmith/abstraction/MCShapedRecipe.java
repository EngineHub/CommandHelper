package com.laytonsmith.abstraction;

import java.util.Map;

public interface MCShapedRecipe extends MCRecipe {
	String getKey();
	Map<Character, MCItemStack> getIngredientMap();
	String[] getShape();
	void setIngredient(char key, MCItemStack ingredient);
	void setShape(String[] shape);
}
