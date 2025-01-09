package com.laytonsmith.abstraction;

public interface MCGrindstoneInventory extends MCInventory {
	MCItemStack getUpperItem();
	MCItemStack getLowerItem();
	MCItemStack getResult();

	void setUpperItem(MCItemStack i);
	void setLowerItem(MCItemStack i);
	void setResult(MCItemStack i);
}
