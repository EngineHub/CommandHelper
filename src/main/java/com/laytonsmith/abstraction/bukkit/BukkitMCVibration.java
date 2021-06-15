package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCLocation;
import org.bukkit.Location;
import org.bukkit.Vibration;
import org.bukkit.entity.Entity;

public class BukkitMCVibration implements AbstractionObject {

	Vibration vibration;

	public BukkitMCVibration(MCLocation origin, MCEntity entity, int arrivalTime) {
		this.vibration = new Vibration((Location) origin.getHandle(),
				new Vibration.Destination.EntityDestination((Entity) entity.getHandle()), arrivalTime);
	}

	public BukkitMCVibration(MCLocation origin, MCLocation location, int arrivalTime) {
		this.vibration = new Vibration((Location) origin.getHandle(),
				new Vibration.Destination.BlockDestination((Location) location.getHandle()), arrivalTime);
	}

	@Override
	public Vibration getHandle() {
		return this.vibration;
	}
}
