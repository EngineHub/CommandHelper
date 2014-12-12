package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCProjectile;
import com.laytonsmith.abstraction.MVector3D;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockProjectileSource;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.enums.MCProjectileType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.util.Vector;

/**
 *
 * @author jb_aero
 */
public class BukkitMCBlockProjectileSource implements MCBlockProjectileSource {

	BlockProjectileSource bps;
	public BukkitMCBlockProjectileSource(BlockProjectileSource source) {
		bps = source;
	}
	
	@Override
	public MCProjectile launchProjectile(MCProjectileType projectile) {
		EntityType et = EntityType.valueOf(projectile.name());
		Class<? extends Entity> c = et.getEntityClass();
		Projectile proj = bps.launchProjectile(c.asSubclass(Projectile.class));

		MCEntity e = BukkitConvertor.BukkitGetCorrectEntity(proj);

		if (e instanceof MCProjectile) {
			return (MCProjectile) e;
		} else {
			return null;
		}
	}

	@Override
	public MCProjectile launchProjectile(MCProjectileType projectile, MVector3D init) {
		EntityType et = EntityType.valueOf(projectile.name());
		Class<? extends Entity> c = et.getEntityClass();
		Vector vector = new Vector(init.x, init.y, init.z);
		Projectile proj = bps.launchProjectile(c.asSubclass(Projectile.class), vector);

		MCEntity e = BukkitConvertor.BukkitGetCorrectEntity(proj);

		if (e instanceof MCProjectile) {
			return (MCProjectile) e;
		} else {
			return null;
		}
	}

	@Override
	public MCBlock getBlock() {
		return new BukkitMCBlock(bps.getBlock());
	}

	@Override
	public Object getHandle() {
		return bps;
	}
}
