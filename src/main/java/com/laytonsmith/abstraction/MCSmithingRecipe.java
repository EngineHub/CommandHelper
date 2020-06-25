package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCMaterial;

public interface MCSmithingRecipe extends MCRecipe {
	MCMaterial[] getBase();
	MCMaterial[] getAddition();
}
