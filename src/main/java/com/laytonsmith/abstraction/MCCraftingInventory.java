package com.laytonsmith.abstraction;

public interface MCCraftingInventory extends MCInventory {

	public MCItemStack[] getMatrix();
	public MCRecipe getRecipe();
	public MCItemStack getResult();
	public void setMatrix(MCItemStack[] contents);
	public void setResult(MCItemStack result);
}
