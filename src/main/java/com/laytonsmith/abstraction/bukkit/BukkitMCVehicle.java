package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCVehicle;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;

/**
 * 
 * @author jb_aero
 */
public class BukkitMCVehicle extends BukkitMCEntity implements MCVehicle {

	@WrappedItem Vehicle v;
	public BukkitMCVehicle(Entity e) {
		super(e);
		this.v = (Vehicle) e;
	}

}
