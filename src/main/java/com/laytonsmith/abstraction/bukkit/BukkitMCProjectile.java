package com.laytonsmith.abstraction.bukkit;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCProjectile;

public class BukkitMCProjectile extends BukkitMCEntity implements MCProjectile {
	
	Projectile proj;
	public BukkitMCProjectile(Entity e) {
		super(e);
		this.proj = (Projectile)e;
	}
	
	public boolean doesBounce() {
		return proj.doesBounce();
	}

	public MCLivingEntity getShooter() {
		MCEntity e = BukkitConvertor.BukkitGetCorrectEntity(proj.getShooter());
		
		if (e instanceof MCLivingEntity) {
			return (MCLivingEntity)e;
		}
		
		return null;
	}

	public void setBounce(boolean doesBounce) {
		proj.setBounce(doesBounce);
	}

	public void setShooter(MCLivingEntity shooter) {
		proj.setShooter(((BukkitMCLivingEntity)shooter).asLivingEntity());
	}

    public Projectile asProjectile() {
        return proj;
    }
}
