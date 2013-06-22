package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.bukkit.BukkitMCVehicle;
import com.laytonsmith.abstraction.entities.MCBoat;
import org.bukkit.entity.Boat;

public class BukkitMCBoat extends BukkitMCVehicle
		implements MCBoat {

	Boat b;
	public BukkitMCBoat(Boat e) {
		super(e);
		this.b = e;
	}

	public double getMaxSpeed() {
		return b.getMaxSpeed();
	}

	public void setMaxSpeed(double speed) {
		b.setMaxSpeed(speed);
	}

	public double getOccupiedDeclaration() {
		return b.getOccupiedDeceleration();
	}

	public void setOccupiedDeclaration(double rate) {
		b.setOccupiedDeceleration(rate);
	}

	public double getUnoccupiedDeclaration() {
		return b.getOccupiedDeceleration();
	}

	public void setUnoccupiedDeclaration(double rate) {
		b.setUnoccupiedDeceleration(rate);
	}

	public boolean getWorkOnLand() {
		return b.getWorkOnLand();
	}

	public void setWorkOnLand(boolean workOnLand) {
		b.setWorkOnLand(workOnLand);
	}
}
