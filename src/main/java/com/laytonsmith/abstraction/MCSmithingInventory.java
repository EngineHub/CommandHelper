package com.laytonsmith.abstraction;

public interface MCSmithingInventory extends MCInventory {
	MCItemStack getInputEquipment();
	MCItemStack getInputMaterial();
	MCItemStack getInputTemplate();
	MCRecipe getRecipe();
	MCItemStack getResult();

	void setInputEquipment(MCItemStack stack);
	void setInputMaterial(MCItemStack stack);
	void setInputTemplate(MCItemStack stack);
	void setResult(MCItemStack stack);
}
