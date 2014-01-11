package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.bukkit.BukkitMCVehicle;
import com.laytonsmith.abstraction.entities.MCMinecart;
import org.bukkit.entity.Minecart;

public class BukkitMCMinecart extends BukkitMCVehicle
		implements MCMinecart {

	Minecart m;
	public BukkitMCMinecart(Minecart e) {
		super(e);
		this.m = e;
	}

	@Override
	public void setDamage(double damage) {
		m.setDamage(damage);
	}

	@Override
	public double getDamage() {
		return m.getDamage();
	}

	@Override
	public double getMaxSpeed() {
		return m.getMaxSpeed();
	}

	@Override
	public void setMaxSpeed(double speed) {
		m.setMaxSpeed(speed);
	}

	@Override
	public boolean isSlowWhenEmpty() {
		return m.isSlowWhenEmpty();
	}

	@Override
	public void setSlowWhenEmpty(boolean slow) {
		m.setSlowWhenEmpty(slow);
	}
}
