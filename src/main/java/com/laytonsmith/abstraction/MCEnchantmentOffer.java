package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCEnchantment;

public interface MCEnchantmentOffer {
	MCEnchantment getEnchantment();

	void setEnchantment(MCEnchantment enchant);

	int getEnchantmentLevel();

	void setEnchantmentLevel(int level);

	int getCost();

	void setCost(int cost);
}
