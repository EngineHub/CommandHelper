
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.enums.MCInventoryType;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
	
	public HashMap<Integer, MCItemStack> addItem(MCItemStack stack) {
		HashMap<Integer, ItemStack> h = i.addItem(stack==null?null:((BukkitMCItemStack)stack).is);
		HashMap<Integer, MCItemStack> m = new HashMap<Integer, MCItemStack>();
		
		for (Map.Entry<Integer, ItemStack> entry : h.entrySet()) {
			Integer key = entry.getKey();
			ItemStack value = entry.getValue();
			m.put(key, new BukkitMCItemStack(value));
		}
		return m;
	}
}
