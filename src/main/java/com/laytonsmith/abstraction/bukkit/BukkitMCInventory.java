package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryHolder;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.enums.MCInventoryType;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.HumanEntity;
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
	
	public void clear() {
		i.clear();
	}
	
	public void clear(int index) {
		i.clear(index);
	}

	public Object getHandle() {
		return i;
	}
	
	@Override
	public String toString() {
		return i.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BukkitMCInventory?i.equals(((BukkitMCInventory)obj).i):false);
	}

	@Override
	public int hashCode() {
		return i.hashCode();
	}

	public List<MCHumanEntity> getViewers() {
		List<MCHumanEntity> retn = new ArrayList<MCHumanEntity>();
		
		for (HumanEntity human: i.getViewers()) {
			retn.add(new BukkitMCHumanEntity((human)));
		}
		
		return retn;
	}
	
	public MCInventoryHolder getHolder() {
		return new BukkitMCInventoryHolder(i.getHolder());
	}
	
	public String getTitle() {
		return i.getTitle();
	}
}
