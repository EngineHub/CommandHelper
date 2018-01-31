package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCVehicle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;

public class BukkitMCVehicle extends BukkitMCEntity implements MCVehicle {

	Vehicle v;
	public BukkitMCVehicle(Entity e) {
		super(e);
		this.v = (Vehicle) e;
	}

}
