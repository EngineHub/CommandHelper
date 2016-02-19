package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCBoat;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;

public class BukkitMCBoat extends BukkitMCVehicle
		implements MCBoat {

	Boat b;

	public BukkitMCBoat(Entity e) {
		super((Vehicle) e);
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

	@Override
	public double getOccupiedDeclaration() {
		return b.getOccupiedDeceleration();
	}

	@Override
	public void setOccupiedDeclaration(double rate) {
		b.setOccupiedDeceleration(rate);
	}

	@Override
	public double getUnoccupiedDeclaration() {
		return b.getOccupiedDeceleration();
	}

	@Override
	public void setUnoccupiedDeclaration(double rate) {
		b.setUnoccupiedDeceleration(rate);
	}

	@Override
	public boolean getWorkOnLand() {
		return b.getWorkOnLand();
	}

	@Override
	public void setWorkOnLand(boolean workOnLand) {
		b.setWorkOnLand(workOnLand);
	}
}
