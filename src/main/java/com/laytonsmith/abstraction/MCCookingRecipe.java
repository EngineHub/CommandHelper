package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCMaterial;

public interface MCCookingRecipe extends MCRecipe {
	MCMaterial[] getInput();
	void setInput(MCItemStack input);
	void setInput(MCMaterial mat);
	void setInput(MCMaterial... mats);
	int getCookingTime();
	void setCookingTime(int ticks);
	float getExperience();
	void setExperience(float exp);
}
