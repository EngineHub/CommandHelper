package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;

public interface MCPrepareItemEnchantEvent extends MCInventoryEvent {

	MCBlock getEnchantBlock();

	MCPlayer getEnchanter();

	int getEnchantmentBonus();

	int[] getExpLevelCostsOffered();

	MCItemStack getItem();

	void setItem(MCItemStack i);
}
