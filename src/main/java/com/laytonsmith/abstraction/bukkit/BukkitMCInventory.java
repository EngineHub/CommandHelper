/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.enums.MCInventoryType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 *
 * @author Layton
 */
public class BukkitMCInventory implements MCInventory {
	private Inventory i;
    public BukkitMCInventory(Inventory inventory) {
        this.i = inventory;
    }

	public MCInventoryType getType() {
		return MCInventoryType.valueOf(this.i.getType().name());
	}

	public int getSize() {
		return this.i.getSize();
	}

	public MCItemStack getItem(int slot) {
        return new BukkitMCItemStack(i.getItem(slot));
    }

    public void setItem(int slot, MCItemStack stack) {
        this.i.setItem(slot, stack==null?null:((BukkitMCItemStack)stack).is);
		if(this.i.getHolder() instanceof Player){
			((Player)this.i.getHolder()).updateInventory();
		}
    }

	public Object getHandle() {
		return i;
	}
}
