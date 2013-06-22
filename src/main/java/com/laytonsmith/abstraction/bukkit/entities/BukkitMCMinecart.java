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

	public void setDamage(int damage) {
		m.setDamage(damage);
	}

	public int getDamage() {
		return m.getDamage();
	}

	public double getMaxSpeed() {
		return m.getMaxSpeed();
	}

	public void setMaxSpeed(double speed) {
		m.setMaxSpeed(speed);
	}

	public boolean isSlowWhenEmpty() {
		return m.isSlowWhenEmpty();
	}

	public void setSlowWhenEmpty(boolean slow) {
		m.setSlowWhenEmpty(slow);
	}
}
