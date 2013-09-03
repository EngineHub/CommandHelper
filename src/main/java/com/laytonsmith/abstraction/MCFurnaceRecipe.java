package com.laytonsmith.abstraction;

public interface MCFurnaceRecipe extends MCRecipe {

	public MCItemStack getInput();
	
	public MCFurnaceRecipe setInput(MCItemStack input);
	
	public MCFurnaceRecipe setInput(int type, int data);
}
