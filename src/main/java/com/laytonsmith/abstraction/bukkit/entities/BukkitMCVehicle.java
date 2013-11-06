package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCVehicle;
import org.bukkit.entity.Vehicle;

/**
 * 
 * @author jb_aero
 */
public abstract class BukkitMCVehicle extends BukkitMCEntity implements MCVehicle {

	public BukkitMCVehicle(Vehicle vehicle) {
		super(vehicle);
	}

	@Override
	public Vehicle getHandle() {
		return (Vehicle) metadatable;
	}
}