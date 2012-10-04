/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCInventoryType;
import com.laytonsmith.abstraction.MCItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Layton
 */
public class BukkitMCDoubleChest extends BukkitMCInventory {
	
	Inventory left;
	Inventory right;
	public BukkitMCDoubleChest(Inventory left, Inventory right){
		super(left);
	}

	@Override
	public int getSize() {
		return left.getSize() + right.getSize();
	}

	@Override
	public MCItemStack getItem(int slot) {
		ItemStack is;
		if(slot < left.getSize()){
			is = left.getItem(slot);
		} else {
			is = right.getItem(slot - left.getSize());
		}
		return new BukkitMCItemStack(is);
	}

	@Override
	public void setItem(int slot, MCItemStack stack) {
		ItemStack is = (ItemStack)((BukkitMCItemStack)stack).getHandle();
		if(slot < left.getSize()){
			left.setItem(slot, is);
		} else {
			right.setItem(slot - left.getSize(), is);
		}
	}
	
}
