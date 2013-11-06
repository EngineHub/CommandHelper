package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCMinecart;
import org.bukkit.entity.Minecart;

public abstract class BukkitMCMinecart extends BukkitMCVehicle implements MCMinecart {

	public BukkitMCMinecart(Minecart minecart) {
		super(minecart);
	}

	@Override
	public Minecart getHandle() {
		return (Minecart) metadatable;
	}

	public void setDamage(double damage) {
		getHandle().setDamage(damage);
	}

	public double getDamage() {
		return getHandle().getDamage();
	}

	public double getMaxSpeed() {
		return getHandle().getMaxSpeed();
	}

	public void setMaxSpeed(double speed) {
		getHandle().setMaxSpeed(speed);
	}

	public boolean isSlowWhenEmpty() {
		return getHandle().isSlowWhenEmpty();
	}

	public void setSlowWhenEmpty(boolean slow) {
		getHandle().setSlowWhenEmpty(slow);
	}
}
