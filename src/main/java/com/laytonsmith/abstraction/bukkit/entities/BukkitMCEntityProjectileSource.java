package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCProjectile;
import com.laytonsmith.abstraction.MCProjectileSource;
import com.laytonsmith.abstraction.Velocity;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.bukkit.BukkitMCEntity;
import com.laytonsmith.abstraction.enums.MCProjectileType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

/**
 * Workaround class to accommodate for the likelihood of non-living entities that can shoot stuff.
 * 
 * @author jb_aero
 */
public class BukkitMCEntityProjectileSource extends BukkitMCEntity implements MCProjectileSource {

	ProjectileSource eps;
	public BukkitMCEntityProjectileSource(Entity source) {
		super(source);
		if (!(source instanceof ProjectileSource)) {
			throw new IllegalArgumentException("Tried to construct BukkitMCEntityProjectileSource from invalid source.");
		}
		eps = (ProjectileSource) source;
	}
	
	@Override
	public MCProjectile launchProjectile(MCProjectileType projectile) {
		EntityType et = EntityType.valueOf(projectile.name());
		Class<? extends Entity> c = et.getEntityClass();
		Projectile proj = eps.launchProjectile(c.asSubclass(Projectile.class));

		MCEntity mcproj = BukkitConvertor.BukkitGetCorrectEntity(proj);

		if (mcproj instanceof MCProjectile) {
			return (MCProjectile) mcproj;
		} else {
			return null;
		}
	}

	@Override
	public MCProjectile launchProjectile(MCProjectileType projectile, Velocity init) {
		EntityType et = EntityType.valueOf(projectile.name());
		Class<? extends Entity> c = et.getEntityClass();
		Vector vector = new Vector(init.x, init.y, init.z);
		Projectile proj = eps.launchProjectile(c.asSubclass(Projectile.class), vector);

		MCEntity mcproj = BukkitConvertor.BukkitGetCorrectEntity(proj);

		if (mcproj instanceof MCProjectile) {
			return (MCProjectile) mcproj;
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return eps.toString();
	}

	@Override
	public int hashCode() {
		return eps.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return eps.equals(obj);
	}
	
	
}
