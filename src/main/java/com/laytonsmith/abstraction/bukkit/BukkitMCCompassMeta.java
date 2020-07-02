package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCCompassMeta;
import com.laytonsmith.abstraction.MCLocation;
import org.bukkit.Location;
import org.bukkit.inventory.meta.CompassMeta;

public class BukkitMCCompassMeta extends BukkitMCItemMeta implements MCCompassMeta {

	CompassMeta cm;

	public BukkitMCCompassMeta(CompassMeta im) {
		super(im);
		this.cm = im;
	}

	@Override
	public MCLocation getTargetLocation() {
		Location l = cm.getLodestone();
		if(l == null) {
			return null;
		}
		return new BukkitMCLocation(l);
	}

	@Override
	public void setTargetLocation(MCLocation location) {
		if(location == null) {
			cm.setLodestone(null);
		} else {
			cm.setLodestone((Location) location.getHandle());
		}
	}

	@Override
	public boolean isLodestoneTracked() {
		return cm.isLodestoneTracked();
	}

	@Override
	public void setLodestoneTracked(boolean tracked) {
		cm.setLodestoneTracked(tracked);
	}
}
