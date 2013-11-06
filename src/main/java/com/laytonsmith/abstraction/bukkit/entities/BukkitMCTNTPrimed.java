package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.TNTPrimed;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.entities.MCEntity;
import com.laytonsmith.abstraction.entities.MCTNTPrimed;

/**
 *
 * @author Layton
 */
public class BukkitMCTNTPrimed extends BukkitMCEntity implements MCTNTPrimed {

	public BukkitMCTNTPrimed(TNTPrimed tnt) {
		super(tnt);
	}

	public BukkitMCTNTPrimed(AbstractionObject ao) {
		this((TNTPrimed) ao.getHandle());
	}

	@Override
	public TNTPrimed getHandle() {
		return (TNTPrimed) metadatable;
	}

	public int getFuseTicks() {
		return getHandle().getFuseTicks();
	}

	public void setFuseTicks(int fuseTicks) {
		getHandle().setFuseTicks(fuseTicks);
	}

	public MCEntity getSource() {
		return BukkitConvertor.BukkitGetCorrectEntity(getHandle().getSource());
	}
}