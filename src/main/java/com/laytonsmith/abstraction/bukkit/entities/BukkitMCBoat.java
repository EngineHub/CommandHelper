package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCBoat;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;

public class BukkitMCBoat extends BukkitMCVehicle implements MCBoat {

	Boat b;

	public BukkitMCBoat(Entity e) {
		super(e);
		this.b = (Boat) e;
	}

	@Override
	public double getMaxSpeed() {
		return b.getMaxSpeed();
	}

	@Override
	public void setMaxSpeed(double speed) {
		b.setMaxSpeed(speed);
	}
}
