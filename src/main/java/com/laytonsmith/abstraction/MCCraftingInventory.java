package com.laytonsmith.abstraction;

public interface MCCraftingInventory extends MCInventory {
	MCItemStack[] getMatrix();
	MCRecipe getRecipe();
	MCItemStack getResult();
	void setMatrix(MCItemStack[] contents);
	void setResult(MCItemStack result);
}
