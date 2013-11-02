package com.laytonsmith.abstraction.entities;

public interface MCMinecart extends MCVehicle {
	public void setDamage(double damage);
	public double getDamage();
	public double getMaxSpeed();
	public void setMaxSpeed(double speed);
	public boolean isSlowWhenEmpty();
	public void setSlowWhenEmpty(boolean slow);
}
