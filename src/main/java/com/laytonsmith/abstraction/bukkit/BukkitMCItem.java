
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionUtils;
import com.laytonsmith.abstraction.MCItem;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.entity.Item;

/**
 *
 * @author Layton
 */
public class BukkitMCItem extends BukkitMCEntity implements MCItem{
	
	@WrappedItem Item i;
	
	public MCItemStack getItemStack(){
		return AbstractionUtils.wrap(i.getItemStack());
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
