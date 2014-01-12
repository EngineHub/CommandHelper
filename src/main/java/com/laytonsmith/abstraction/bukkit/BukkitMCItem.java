
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCItem;
import com.laytonsmith.abstraction.MCItemStack;
import org.bukkit.entity.Item;

/**
 *
 * @author Layton
 */
public class BukkitMCItem extends BukkitMCEntity implements MCItem{
	
	Item i;
	
	public BukkitMCItem(Item i){
		super(i);
		this.i = i;
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
