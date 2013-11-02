package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCBoat;

import org.bukkit.entity.Boat;

public class BukkitMCBoat extends BukkitMCVehicle implements MCBoat {

	public BukkitMCBoat(Boat boat) {
		super(boat);
	}

	public BukkitMCBoat(AbstractionObject ao) {
		this((Boat) ao.getHandle());
	}

	@Override
	public Boat getHandle() {
		return (Boat) metadatable;
	}

	public double getMaxSpeed() {
		return getHandle().getMaxSpeed();
	}

	public void setMaxSpeed(double speed) {
		getHandle().setMaxSpeed(speed);
	}

	public double getOccupiedDeclaration() {
		return getHandle().getOccupiedDeceleration();
	}

	public void setOccupiedDeclaration(double rate) {
		getHandle().setOccupiedDeceleration(rate);
	}

	public double getUnoccupiedDeclaration() {
		return getHandle().getOccupiedDeceleration();
	}

	public void setUnoccupiedDeclaration(double rate) {
		getHandle().setUnoccupiedDeceleration(rate);
	}

	public boolean getWorkOnLand() {
		return getHandle().getWorkOnLand();
	}

	public void setWorkOnLand(boolean workOnLand) {
		getHandle().setWorkOnLand(workOnLand);
	}
}