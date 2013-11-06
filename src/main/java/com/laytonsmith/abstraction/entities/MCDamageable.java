package com.laytonsmith.abstraction.entities;

/**
 *
 * @author Hekta
 */
public interface MCDamageable extends MCEntity {

	public double getHealth();
	public void setHealth(double health);

	public double getMaxHealth();
	public void setMaxHealth(double health);
	public void resetMaxHealth();

	public void damage(double amount);
	public void damage(double amount, MCEntity source);
}