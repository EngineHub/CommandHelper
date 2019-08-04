package com.laytonsmith.abstraction;

public interface MCEnchantmentOffer {
	MCEnchantment getEnchantment();

	void setEnchantment(MCEnchantment enchant);

	int getEnchantmentLevel();

	void setEnchantmentLevel(int level);

	int getCost();

	void setCost(int cost);
}
