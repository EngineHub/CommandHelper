/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;

/**
 *
 * @author cgallarno
 */
public interface MCPrepareItemEnchantEvent extends MCInventoryEvent {
	public MCBlock getEnchantBlock();
	public MCPlayer getEnchanter();
	public int getEnchantmentBonus();
	public int[] getExpLevelCostsOffered();
	public MCItemStack getItem();
	public void setItem(MCItemStack i);
}
