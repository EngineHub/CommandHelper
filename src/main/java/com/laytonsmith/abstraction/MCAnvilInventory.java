package com.laytonsmith.abstraction;

public interface MCAnvilInventory extends MCInventory {
	MCItemStack getFirstItem();
	MCItemStack getSecondItem();
	MCItemStack getResult();
	int getMaximumRepairCost();
	int getRepairCost();
	int getRepairCostAmount();
	String getRenameText();

	void setFirstItem(MCItemStack stack);
	void setSecondItem(MCItemStack stack);
	void setResult(MCItemStack stack);
	void setMaximumRepairCost(int levels);
	void setRepairCost(int levels);
	void setRepairCostAmount(int levels);
}
