package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEnchantmentOffer;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;

public interface MCPrepareItemEnchantEvent extends MCInventoryEvent {

	MCBlock getEnchantBlock();

	MCPlayer getEnchanter();

	int getEnchantmentBonus();

	MCEnchantmentOffer[] getOffers();

	MCItemStack getItem();

	void setItem(MCItemStack i);
}
