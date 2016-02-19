package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.abstraction.MCFireball;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.util.Vector;

/**
 * 
 * @author jb_aero
 */
public class BukkitMCFireball extends BukkitMCProjectile implements MCFireball {

	Fireball f;

	public BukkitMCFireball(Entity be) {
		super(be);
		f = (Fireball) be;
	}

	@Override
	public Vector3D getDirection() {
		return new Vector3D(f.getDirection().getX(), f.getDirection().getY(), f.getDirection().getZ());
	}

	@Override
	public void setDirection(Vector3D vector) {
		f.setDirection(new Vector(vector.X(), vector.Y(), vector.Z()));
	}

}
