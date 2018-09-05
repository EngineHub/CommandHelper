package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryHolder;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCHumanEntity;
import com.laytonsmith.abstraction.enums.MCInventoryType;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.CHLog.Tags;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.functions.InventoryManagement;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BukkitMCInventory implements MCInventory {

	private final Inventory i;

	public BukkitMCInventory(Inventory inventory) {
		this.i = inventory;
	}

	@Override
	public MCInventoryType getType() {
		return MCInventoryType.valueOf(this.i.getType().name());
	}

	@Override
	public int getSize() {
		return this.i.getSize();
	}

	@Override
	public MCItemStack getItem(int slot) {
		try {
			return new BukkitMCItemStack(i.getItem(slot));
		} catch (ArrayIndexOutOfBoundsException aioobe) {
			if(slot > 0 && slot < getSize()) {
				CHLog.GetLogger().Log(Tags.RUNTIME, LogLevel.WARNING, "The API claims that a particular slot is"
						+ " accessible, however the server implementation does not give access."
						+ " This is the fault of the server and can't be helped by "
						+ Implementation.GetServerType().getBranding() + ".", Target.UNKNOWN);
			} else {
				throw new CRERangeException("No slot " + slot + " exists in the given inventory", Target.UNKNOWN);
			}
			return null;
		}
	}

	@Override
	public void setItem(int slot, MCItemStack stack) {
		try {
			this.i.setItem(slot, stack == null ? null : ((BukkitMCItemStack) stack).is);
		} catch (ArrayIndexOutOfBoundsException aioobe) {
			if(slot > 0 && slot < getSize()) {
				CHLog.GetLogger().Log(Tags.RUNTIME, LogLevel.WARNING, "The API claims that a particular slot is"
						+ " accessible, however the server implementation does not give access."
						+ " This is the fault of the server and can't be helped by "
						+ Implementation.GetServerType().getBranding() + ".", Target.UNKNOWN);
			} else {
				throw new CRERangeException("No slot " + slot + " exists in the given inventory", Target.UNKNOWN);
			}
		}
	}

	@Override
	public void clear() {
		i.clear();
	}

	@Override
	public void clear(int index) {
		i.clear(index);
	}

	@Override
	public Object getHandle() {
		return i;
	}

	@Override
	public String toString() {
		return i.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof BukkitMCInventory && i.equals(((BukkitMCInventory) obj).i);
	}

	@Override
	public int hashCode() {
		return i.hashCode();
	}

	@Override
	public Map<Integer, MCItemStack> addItem(MCItemStack stack) {
		Map<Integer, ItemStack> h = i.addItem(stack == null ? null : ((BukkitMCItemStack) stack).is);
		Map<Integer, MCItemStack> m = new HashMap<>();
		for(Map.Entry<Integer, ItemStack> entry : h.entrySet()) {
			Integer key = entry.getKey();
			ItemStack value = entry.getValue();
			m.put(key, new BukkitMCItemStack(value));
		}
		return m;
	}

	@Override
	public List<MCHumanEntity> getViewers() {
		List<MCHumanEntity> retn = new ArrayList<>();
		for(HumanEntity human : i.getViewers()) {
			retn.add(new BukkitMCHumanEntity((human)));
		}
		return retn;
	}

	@Override
	public void updateViewers() {
		for(HumanEntity human : i.getViewers()) {
			if(human instanceof Player) {
				((Player) human).updateInventory();
			}
		}
	}

	@Override
	public MCInventoryHolder getHolder() {
		InventoryHolder ih = i.getHolder();
		if(ih instanceof BlockState) {
			return (MCInventoryHolder) BukkitConvertor.BukkitGetCorrectBlockState((BlockState) ih);
		} else if(ih instanceof Entity) {
			return (MCInventoryHolder) BukkitConvertor.BukkitGetCorrectEntity((Entity) ih);
		} else if(ih instanceof BukkitMCVirtualInventoryHolder.VirtualHolder) {
			return new BukkitMCVirtualInventoryHolder(ih);
		} else if(ih instanceof DoubleChest) {
			return new BukkitMCDoubleChest((DoubleChest) ih);
		} else if(ih == null) {
			for(Map.Entry<String, MCInventory> entry : InventoryManagement.VIRTUAL_INVENTORIES.entrySet()) {
				if(entry.getValue().equals(this)) {
					return new BukkitMCVirtualInventoryHolder(entry.getKey());
				}
			}
		}
		return new BukkitMCInventoryHolder(ih);
	}

	@Override
	public String getTitle() {
		return i.getTitle();
	}
}
