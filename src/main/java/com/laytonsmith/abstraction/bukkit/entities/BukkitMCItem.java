
package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCItem;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

/**
 *
 * 
 */
public class BukkitMCItem extends BukkitMCEntity implements MCItem{
	
	Item i;

	public BukkitMCItem(Entity i) {
		super(i);
		this.i = (Item) i;
	}
	
	@Override
	public MCItemStack getItemStack(){
		return new BukkitMCItemStack(i.getItemStack());
	}
	
	@Override
	public int getPickupDelay(){
		return i.getPickupDelay();
	}
	
	@Override
	public void setItemStack(MCItemStack stack){
		i.setItemStack(((BukkitMCItemStack) stack).asItemStack());
	}
	
	@Override
	public void setPickupDelay(int delay){
		i.setPickupDelay(delay);
	}
}
