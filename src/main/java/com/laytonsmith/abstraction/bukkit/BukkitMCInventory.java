package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionUtils;
import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryHolder;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.enums.MCInventoryType;
import com.laytonsmith.annotations.WrappedItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Layton
 */
public class BukkitMCInventory implements MCInventory {
	@WrappedItem Inventory i;
	
	protected BukkitMCInventory(Inventory i){
		this.i = i;
	}
	
	protected BukkitMCInventory(){
		
	}

	public MCInventoryType getType() {
		return MCInventoryType.valueOf(this.i.getType().name());
	}

	public int getSize() {
		return this.i.getSize();
	}

	public MCItemStack getItem(int slot) {
        return AbstractionUtils.wrap(i.getItem(slot));
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
	
	public Map<Integer, MCItemStack> addItem(MCItemStack stack) {
		Map<Integer, ItemStack> h = i.addItem(stack==null?null:((BukkitMCItemStack)stack).is);
		Map<Integer, MCItemStack> m = new HashMap<Integer, MCItemStack>();
		
		for (Map.Entry<Integer, ItemStack> entry : h.entrySet()) {
			Integer key = entry.getKey();
			ItemStack value = entry.getValue();
			m.put(key, (MCItemStack) AbstractionUtils.wrap(value));
		}
		return m;
	}

	public List<MCHumanEntity> getViewers() {
		List<MCHumanEntity> retn = new ArrayList<MCHumanEntity>();
		
		for (HumanEntity human: i.getViewers()) {
			retn.add((MCHumanEntity)AbstractionUtils.wrap(human));
		}
		
		return retn;
	}
	
	public MCInventoryHolder getHolder() {
		return AbstractionUtils.wrap(i.getHolder());
	}
	
	public String getTitle() {
		return i.getTitle();
	}
}
