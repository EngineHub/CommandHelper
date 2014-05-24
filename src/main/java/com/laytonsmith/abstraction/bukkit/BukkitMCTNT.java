
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCTNT;
import org.bukkit.entity.TNTPrimed;

/**
 *
 * 
 */
public class BukkitMCTNT extends BukkitMCEntity implements MCTNT {
	TNTPrimed tnt;
	public BukkitMCTNT(TNTPrimed e) {
		super(e);
		this.tnt = e;
	}	

	@Override
	public MCEntity getSource() {
		return new BukkitMCEntity(tnt.getSource());
	}

	@Override
	public int getFuseTicks() {
		return tnt.getFuseTicks();
	}

	@Override
	public void setFuseTicks(int ticks) {
		tnt.setFuseTicks(ticks);
	}
}
