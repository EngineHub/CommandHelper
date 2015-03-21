package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCProjectile;
import com.laytonsmith.abstraction.MCProjectileSource;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlockProjectileSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;

public class BukkitMCProjectile extends BukkitMCEntity implements MCProjectile {
	
	Projectile proj;

	public BukkitMCProjectile(Entity e) {
		super(e);
		this.proj = (Projectile) e;
	}
	
	@Override
	public boolean doesBounce() {
		return proj.doesBounce();
	}

	@Override
	public MCProjectileSource getShooter() {
		ProjectileSource source = proj.getShooter();
		
		if (source instanceof BlockProjectileSource) {
			return new BukkitMCBlockProjectileSource((BlockProjectileSource) source);
		}
		
		if (source instanceof Entity) {
			MCEntity e = BukkitConvertor.BukkitGetCorrectEntity((Entity) source);
			if (e instanceof MCProjectileSource) {
				return (MCProjectileSource) e;
			}
		}
		
		return null;
	}

	@Override
	public void setBounce(boolean doesBounce) {
		proj.setBounce(doesBounce);
	}

	@Override
	public void setShooter(MCProjectileSource shooter) {
		proj.setShooter((ProjectileSource) shooter.getHandle());
	}

	public Projectile asProjectile() {
		return proj;
	}
}
