

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.AbstractionUtils;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayerInventory;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

/**
 *
 * @author layton
 */
public class BukkitMCPlayerInventory extends BukkitMCInventory implements MCPlayerInventory {

    @WrappedItem private PlayerInventory i;

    public void setHelmet(MCItemStack stack) {
        this.i.setHelmet(((BukkitMCItemStack)stack).is);
		if(this.i.getHolder() instanceof Player){
			((Player)this.i.getHolder()).updateInventory();
		}
    }

    public void setChestplate(MCItemStack stack) {
        this.i.setChestplate(((BukkitMCItemStack)stack).is);
		if(this.i.getHolder() instanceof Player){
			((Player)this.i.getHolder()).updateInventory();
		}
    }

    public void setLeggings(MCItemStack stack) {
        this.i.setLeggings(((BukkitMCItemStack)stack).is);
		if(this.i.getHolder() instanceof Player){
			((Player)this.i.getHolder()).updateInventory();
		}
    }

    public void setBoots(MCItemStack stack) {
        this.i.setBoots(((BukkitMCItemStack)stack).is);
		if(this.i.getHolder() instanceof Player){
			((Player)this.i.getHolder()).updateInventory();
		}
    }

    public MCItemStack getHelmet() {
        return AbstractionUtils.wrap(this.i.getHelmet());
    }

    public MCItemStack getChestplate() {
        return AbstractionUtils.wrap(this.i.getChestplate());
    }

    public MCItemStack getLeggings() {
        return AbstractionUtils.wrap(this.i.getLeggings());
    }

    public MCItemStack getBoots() {
        return AbstractionUtils.wrap(this.i.getBoots());
    }

	public int getHeldItemSlot() {
		return i.getHeldItemSlot();
	}
}
