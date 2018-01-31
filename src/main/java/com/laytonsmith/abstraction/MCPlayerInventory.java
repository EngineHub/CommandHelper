package com.laytonsmith.abstraction;

public interface MCPlayerInventory extends MCInventory {
	void setHelmet(MCItemStack stack);
	void setChestplate(MCItemStack stack);
	void setLeggings(MCItemStack stack);
	void setBoots(MCItemStack stack);
	void setItemInOffHand(MCItemStack stack);
	MCItemStack getHelmet();
	MCItemStack getChestplate();
	MCItemStack getLeggings();
	MCItemStack getBoots();
	MCItemStack getItemInOffHand();
	int getHeldItemSlot();
	void setHeldItemSlot(int slot);
}
