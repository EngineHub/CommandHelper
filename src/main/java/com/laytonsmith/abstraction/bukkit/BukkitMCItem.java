
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCItem;
import com.laytonsmith.abstraction.MCItemStack;
import org.bukkit.entity.Item;

/**
 *
 */
public class BukkitMCItem extends BukkitMCEntity implements MCItem{
	
	Item i;
	
	public BukkitMCItem(Item i){
		super(i);
		this.i = i;
	}
	
	public MCItemStack getItemStack(){
		return new BukkitMCItemStack(i.getItemStack());
	}
	
	public int getPickupDelay(){
		return i.getPickupDelay();
	}
	
	public void setItemStack(MCItemStack stack){
		i.setItemStack(((BukkitMCItemStack) stack).asItemStack());
	}
	
	public void setPickupDelay(int delay){
		i.setPickupDelay(delay);
	}
}
