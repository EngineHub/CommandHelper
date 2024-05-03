package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.abstraction.entities.MCProjectile;
import com.laytonsmith.abstraction.MCProjectileSource;
import com.laytonsmith.abstraction.enums.MCEntityType;
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
		if(!(source instanceof ProjectileSource)) {
			throw new IllegalArgumentException("Tried to construct BukkitMCEntityProjectileSource from invalid source.");
		}
		eps = (ProjectileSource) source;
	}

	@Override
	public MCProjectile launchProjectile(MCEntityType projectile) {
		EntityType et = (EntityType) projectile.getConcrete();
		Class<? extends Projectile> p = et.getEntityClass().asSubclass(Projectile.class);
		return new BukkitMCProjectile(eps.launchProjectile(p));
	}

	@Override
	public MCProjectile launchProjectile(MCEntityType projectile, Vector3D init) {
		EntityType et = (EntityType) projectile.getConcrete();
		Vector vector = new Vector(init.X(), init.Y(), init.Z());
		Class<? extends Projectile> p = et.getEntityClass().asSubclass(Projectile.class);
		return new BukkitMCProjectile(eps.launchProjectile(p, vector));
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
		return obj instanceof BukkitMCEntityProjectileSource && eps.equals(((BukkitMCEntityProjectileSource) obj).eps);
	}
}
