package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCBoat;
import com.laytonsmith.abstraction.enums.MCTreeSpecies;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCTreeSpecies;
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

	@Override
	public MCTreeSpecies getWoodType() {
		return BukkitMCTreeSpecies.getConvertor().getAbstractedEnum(b.getWoodType());
	}

	@Override
	public void setWoodType(MCTreeSpecies type) {
		b.setWoodType(BukkitMCTreeSpecies.getConvertor().getConcreteEnum(type));
	}

}
