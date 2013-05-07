package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCRepairable;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.inventory.meta.Repairable;

/**
 *
 * @author jb_aero
 */
public class BukkitMCRepairable implements MCRepairable {

	@WrappedItem Repairable r;

	public boolean hasRepairCost() {
		return r.hasRepairCost();
	}

	public int getRepairCost() {
		return r.getRepairCost();
	}

	public void setRepairCost(int cost) {
		r.setRepairCost(cost);
	}

	public <T> T getHandle() {
		return (T) r;
	}

}
