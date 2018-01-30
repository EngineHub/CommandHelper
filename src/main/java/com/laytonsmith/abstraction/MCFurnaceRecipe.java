package com.laytonsmith.abstraction;

public interface MCFurnaceRecipe extends MCRecipe {
	MCItemStack getInput();
	void setInput(MCItemStack input);
}
