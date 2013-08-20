package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCRecipeType;

public interface MCRecipe extends AbstractionObject {
	public MCItemStack getResult();
	public MCRecipeType getRecipeType();
}
