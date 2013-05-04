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
	public BukkitMCRepairable(Repairable rep) {
		this.r = rep;
	}

	public BukkitMCRepairable(AbstractionObject o) {
		this.r = (Repairable) o;
	}

	public boolean hasRepairCost() {
		return r.hasRepairCost();
	}

	public int getRepairCost() {
		return r.getRepairCost();
	}

	public void setRepairCost(int cost) {
		r.setRepairCost(cost);
	}

}
