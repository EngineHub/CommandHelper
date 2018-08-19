package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCMaterial;

public interface MCFurnaceRecipe extends MCRecipe {

	String getKey();

	MCItemStack getInput();

	void setInput(MCItemStack input);

	void setInput(MCMaterial mat);
}
