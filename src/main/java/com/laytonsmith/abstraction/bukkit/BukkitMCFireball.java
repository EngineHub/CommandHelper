package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCFireball;
import org.bukkit.entity.Fireball;
import org.bukkit.util.Vector;

/**
 * 
 * @author jb_aero
 */
public class BukkitMCFireball extends BukkitMCProjectile implements MCFireball {

	Fireball f;
	public BukkitMCFireball(Fireball be) {
		super(be);
		f = be;
	}

	@Override
	public Velocity getDirection() {
		return new Velocity(1, f.getDirection().getX(), f.getDirection().getY(), f.getDirection().getZ());
	}

	@Override
	public void setDirection(Velocity vector) {
		f.setDirection(new Vector(vector.x, vector.y, vector.z));
	}

}
