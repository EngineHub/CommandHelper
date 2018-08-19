package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCMaterial;

import java.util.Map;

public interface MCShapedRecipe extends MCRecipe {

	String getKey();

	Map<Character, MCItemStack> getIngredientMap();

	String[] getShape();

	void setIngredient(char key, MCItemStack ingredient);

	void setIngredient(char key, MCMaterial mat);

	void setShape(String[] shape);
}
