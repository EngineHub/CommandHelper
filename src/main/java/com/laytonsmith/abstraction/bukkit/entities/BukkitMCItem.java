package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCItem;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import org.bukkit.entity.Item;

/**
 *
 * @author Layton
 */
public class BukkitMCItem extends BukkitMCEntity implements MCItem {

	public BukkitMCItem(Item item) {
		super(item);
	}

	public BukkitMCItem(AbstractionObject ao) {
		this((Item) ao.getHandle());
	}

	@Override
	public Item getHandle() {
		return (Item) metadatable;
	}
	
	public MCItemStack getItemStack(){
		return new BukkitMCItemStack(getHandle().getItemStack());
	}
	
	public int getPickupDelay(){
		return getHandle().getPickupDelay();
	}
	
	public void setItemStack(MCItemStack stack){
		getHandle().setItemStack(((BukkitMCItemStack) stack).asItemStack());
	}
	
	public void setPickupDelay(int delay){
		getHandle().setPickupDelay(delay);
	}
}
