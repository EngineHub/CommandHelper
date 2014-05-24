package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import java.util.Map;

/**
 *
 * @author cgallarno
 */
public interface MCEnchantItemEvent extends MCInventoryEvent {
	public MCBlock getEnchantBlock();
	public MCPlayer GetEnchanter();
	public Map<MCEnchantment, Integer> getEnchantsToAdd();
	public void setEnchantsToAdd(Map<MCEnchantment, Integer> enchants);
	public MCItemStack getItem();
	public void setItem(MCItemStack i);
	public void setExpLevelCost(int level);
	public int getExpLevelCost();
	public int whichButton();
}
