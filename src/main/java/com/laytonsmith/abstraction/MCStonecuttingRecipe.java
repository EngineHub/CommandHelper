package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCMaterial;

public interface MCStonecuttingRecipe extends MCRecipe {
	MCRecipeChoice getInput();
	void setInput(MCItemStack input);
	void setInput(MCMaterial mat);
	void setInput(MCRecipeChoice choice);
}
