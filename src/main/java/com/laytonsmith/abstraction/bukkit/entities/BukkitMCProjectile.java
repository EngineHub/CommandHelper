package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.entities.MCProjectile;
import com.laytonsmith.abstraction.MCProjectileSource;
import com.laytonsmith.abstraction.blocks.MCBlockProjectileSource;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlockProjectileSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;

public class BukkitMCProjectile extends BukkitMCEntity implements MCProjectile {

	public BukkitMCProjectile(Entity e) {
		super(e);
	}

	@Override
	public boolean doesBounce() {
		// Some entities (like fireworks prior to 1.16) may not be treated as projectiles on this server implementation
		if(getHandle() instanceof Projectile) {
			return ((Projectile) getHandle()).doesBounce();
		}
		return false;
	}

	@Override
	public MCProjectileSource getShooter() {
		if(getHandle() instanceof Projectile) {
			ProjectileSource source = ((Projectile) getHandle()).getShooter();

			if(source instanceof BlockProjectileSource) {
				return new BukkitMCBlockProjectileSource((BlockProjectileSource) source);
			}

			if(source instanceof Entity) {
				MCEntity e = BukkitConvertor.BukkitGetCorrectEntity((Entity) source);
				if(e instanceof MCProjectileSource) {
					return (MCProjectileSource) e;
				}
			}
		}
		return null;
	}

	@Override
	public void setBounce(boolean doesBounce) {
		if(getHandle() instanceof Projectile) {
			((Projectile) getHandle()).setBounce(doesBounce);
		}
	}

	@Override
	public void setShooter(MCProjectileSource shooter) {
		if(getHandle() instanceof Projectile) {
			if(shooter == null) {
				((Projectile) getHandle()).setShooter(null);
			} else if(shooter instanceof MCBlockProjectileSource) {
				((Projectile) getHandle()).setShooter((BlockProjectileSource) shooter.getHandle());
			} else {
				((Projectile) getHandle()).setShooter((ProjectileSource) shooter.getHandle());
			}
		}
	}
}
