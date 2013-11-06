package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCFireball;
import com.laytonsmith.abstraction.MCVector;
import com.laytonsmith.abstraction.bukkit.BukkitMCVector;
import org.bukkit.entity.Fireball;
import org.bukkit.util.Vector;

/**
 * 
 * @author jb_aero
 */
public abstract class BukkitMCFireball extends BukkitMCProjectile implements MCFireball {

	public BukkitMCFireball(Fireball fireball) {
		super(fireball);
	}

	@Override
	public Fireball getHandle() {
		return (Fireball) metadatable;
	}

	public MCVector getDirection() {
		return new BukkitMCVector(getHandle().getDirection());
	}

	public void setDirection(MCVector vector) {
		getHandle().setDirection((Vector) vector.getHandle());
	}
}