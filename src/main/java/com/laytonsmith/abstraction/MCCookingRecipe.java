package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCMaterial;

public interface MCCookingRecipe extends MCRecipe {
	MCRecipeChoice getInput();
	void setInput(MCItemStack input);
	void setInput(MCMaterial mat);
	void setInput(MCRecipeChoice choice);
	int getCookingTime();
	void setCookingTime(int ticks);
	float getExperience();
	void setExperience(float exp);
}
