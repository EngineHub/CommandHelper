package com.laytonsmith.abstraction.bukkit;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;

import com.laytonsmith.abstraction.MCVehicle;

/**
 * 
 * @author jb_aero
 */
public class BukkitMCVehicle extends BukkitMCEntity implements MCVehicle {

	Vehicle v;
	public BukkitMCVehicle(Entity e) {
		super(e);
		this.v = (Vehicle) e;
	}

}
