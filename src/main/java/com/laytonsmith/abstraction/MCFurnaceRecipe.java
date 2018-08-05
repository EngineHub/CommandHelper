package com.laytonsmith.abstraction;

public interface MCFurnaceRecipe extends MCRecipe {

	String getKey();

	MCItemStack getInput();

	void setInput(MCItemStack input);
}
