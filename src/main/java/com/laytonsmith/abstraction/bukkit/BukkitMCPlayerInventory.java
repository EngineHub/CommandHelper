

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayerInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

/**
 *
 * @author layton
 */
public class BukkitMCPlayerInventory extends BukkitMCInventory implements MCPlayerInventory {

    private PlayerInventory i;
    public BukkitMCPlayerInventory(PlayerInventory inventory) {
		super(inventory);
        this.i = inventory;
    }
    
    public BukkitMCPlayerInventory(AbstractionObject a){
        this((PlayerInventory)null);
        if(a instanceof MCPlayerInventory){
            this.i = ((PlayerInventory)a.getHandle());
        } else {
            throw new ClassCastException();
        }
    }

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
        return new BukkitMCItemStack(this.i.getHelmet());
    }

    public MCItemStack getChestplate() {
        return new BukkitMCItemStack(this.i.getChestplate());
    }

    public MCItemStack getLeggings() {
        return new BukkitMCItemStack(this.i.getLeggings());
    }

    public MCItemStack getBoots() {
        return new BukkitMCItemStack(this.i.getBoots());
    }

	public int getHeldItemSlot() {
		return i.getHeldItemSlot();
	}
	
	public void setHeldItemSlot(int slot) {
		i.setHeldItemSlot(slot);
	}
}
