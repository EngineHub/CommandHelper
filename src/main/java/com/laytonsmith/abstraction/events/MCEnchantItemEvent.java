package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import java.util.Map;

public interface MCEnchantItemEvent extends MCInventoryEvent {

	MCBlock getEnchantBlock();

	MCPlayer GetEnchanter();

	Map<MCEnchantment, Integer> getEnchantsToAdd();

	void setEnchantsToAdd(Map<MCEnchantment, Integer> enchants);

	MCItemStack getItem();

	void setItem(MCItemStack i);

	void setExpLevelCost(int level);

	int getExpLevelCost();

	int whichButton();
}
