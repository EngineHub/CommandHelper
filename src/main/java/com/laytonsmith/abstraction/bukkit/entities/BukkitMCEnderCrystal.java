package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCEnderCrystal;
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
		try {
			return ec.isShowingBottom();
		} catch (NoSuchMethodError ex) {
			// Probably 1.8.9 or prior
			return true;
		}
	}

	@Override
	public void setShowingBottom(boolean showing) {
		try {
			ec.setShowingBottom(showing);
		} catch (NoSuchMethodError ex) {
			// Probably 1.8.9 or prior
		}
	}

	@Override
	public MCLocation getBeamTarget() {
		try {
			Location target = ec.getBeamTarget();
			if (target == null) {
				return null;
			}
			return new BukkitMCLocation(target);
		} catch (NoSuchMethodError ex) {
			// Probably 1.8.9 or prior
			return null;
		}
	}

	@Override
	public void setBeamTarget(MCLocation target) {
		try {
			if (target == null) {
				ec.setBeamTarget(null);
			} else {
				ec.setBeamTarget((Location) target.getHandle());
			}
		} catch (NoSuchMethodError ex) {
			// Probably 1.8.9 or prior
		}
	}
}
