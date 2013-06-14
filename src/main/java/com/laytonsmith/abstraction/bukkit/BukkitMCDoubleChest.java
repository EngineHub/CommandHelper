
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
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
	
	@Override
	public String toString() {
		return left.toString() + ":" + right.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final BukkitMCDoubleChest other = (BukkitMCDoubleChest) obj;
		if (this.left != other.left && (this.left == null || !this.left.equals(other.left))) {
			return false;
		}
		if (this.right != other.right && (this.right == null || !this.right.equals(other.right))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 59 * hash + (this.left != null ? this.left.hashCode() : 0);
		hash = 59 * hash + (this.right != null ? this.right.hashCode() : 0);
		return hash;
	}
	
}
