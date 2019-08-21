package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCEnderCrystal;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import org.bukkit.Location;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;

public class BukkitMCEnderCrystal extends BukkitMCEntity implements MCEnderCrystal {

	EnderCrystal ec;

	public BukkitMCEnderCrystal(Entity ec) {
		super(ec);
		this.ec = (EnderCrystal) ec;
	}

	@Override
	public boolean isShowingBottom() {
		return ec.isShowingBottom();
	}

	@Override
	public void setShowingBottom(boolean showing) {
		ec.setShowingBottom(showing);
	}

	@Override
	public MCLocation getBeamTarget() {
		Location target = ec.getBeamTarget();
		if(target == null) {
			return null;
		}
		return new BukkitMCLocation(target);
	}

	@Override
	public void setBeamTarget(MCLocation target) {
		if(target == null) {
			ec.setBeamTarget(null);
		} else {
			ec.setBeamTarget((Location) target.getHandle());
		}
	}
}
