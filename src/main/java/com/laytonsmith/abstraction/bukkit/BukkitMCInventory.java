package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryHolder;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.enums.MCInventoryType;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.CHLog.Tags;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.functions.Exceptions;

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
		try {
			return new BukkitMCItemStack(i.getItem(slot));
		} catch (ArrayIndexOutOfBoundsException aioobe) {
			if (slot > 0 && slot < getSize()) {
				CHLog.GetLogger().Log(Tags.RUNTIME, LogLevel.WARNING, "The API claims that a particular slot is"
						+ " accessible, however the server implementation does not give access."
						+ " This is the fault of the server and can't be helped by "
						+ Implementation.GetServerType().getBranding() + ".", Target.UNKNOWN);
			} else {
				throw new Exceptions.RangeException("No slot " + slot + " exists in the given inventory", Target.UNKNOWN);
			}
			return null;
		}
    }

	public void setItem(int slot, MCItemStack stack) {
		try {
			this.i.setItem(slot, stack == null ? null : ((BukkitMCItemStack) stack).is);
		} catch (ArrayIndexOutOfBoundsException aioobe) {
			if (slot > 0 && slot < getSize()) {
				CHLog.GetLogger().Log(Tags.RUNTIME, LogLevel.WARNING, "The API claims that a particular slot is"
						+ " accessible, however the server implementation does not give access."
						+ " This is the fault of the server and can't be helped by "
						+ Implementation.GetServerType().getBranding() + ".", Target.UNKNOWN);
			} else {
				throw new Exceptions.RangeException("No slot " + slot + " exists in the given inventory", Target.UNKNOWN);
			}
		}
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
			m.put(key, new BukkitMCItemStack(value));
		}
		return m;
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
