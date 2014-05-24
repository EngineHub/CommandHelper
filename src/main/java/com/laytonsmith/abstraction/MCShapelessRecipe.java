package com.laytonsmith.abstraction;

import java.util.List;

/**
 * 
 * @author jb_aero
 */
public interface MCShapelessRecipe extends MCRecipe {

	public MCShapelessRecipe addIngredient(MCItemStack ingredient);
	
	public MCShapelessRecipe addIngredient(int type, int data, int amount);
	
	public List<MCItemStack> getIngredients();
	
	public MCShapelessRecipe removeIngredient(MCItemStack ingredient);
	
	public MCShapelessRecipe removeIngredient(int type, int data, int amount);
}
