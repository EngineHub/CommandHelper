package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;

import com.laytonsmith.abstraction.entities.MCDamageable;
import com.laytonsmith.abstraction.entities.MCEntity;

/**
 *
 * @author Hekta
 */
public abstract class BukkitMCDamageable extends BukkitMCEntity implements MCDamageable {

	public BukkitMCDamageable(Damageable damageable) {
		super(damageable);
	}

	@Override
	public Damageable getHandle() {
		return (Damageable) metadatable;
	}

	public double getHealth() {
		return getHandle().getHealth();
	}

	public void setHealth(double health) {
		getHandle().setHealth(health);
	}

	public double getMaxHealth() {
		return getHandle().getMaxHealth();
	}
	
	public void setMaxHealth(double health) {
		getHandle().setMaxHealth(health);
	}
	
	public void resetMaxHealth() {
		getHandle().resetMaxHealth();
	}

	public void damage(double amount) {
		getHandle().damage(amount);
	}

	public void damage(double amount, MCEntity source) {
		getHandle().damage(amount, (Entity) source.getHandle());
	}
}