package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCProjectile;
import org.bukkit.entity.Projectile;

public class BukkitMCProjectile extends BukkitMCEntity implements MCProjectile {
	
	Projectile proj;
	public BukkitMCProjectile(Projectile e) {
		super(e);
		this.proj = e;
	}
	
	@Override
	public boolean doesBounce() {
		return proj.doesBounce();
	}

	@Override
	public MCLivingEntity getShooter() {
		MCEntity e = BukkitConvertor.BukkitGetCorrectEntity(proj.getShooter());
		
		if (e instanceof MCLivingEntity) {
			return (MCLivingEntity)e;
		}
		
		return null;
	}

	@Override
	public void setBounce(boolean doesBounce) {
		proj.setBounce(doesBounce);
	}

	@Override
	public void setShooter(MCLivingEntity shooter) {
		proj.setShooter(((BukkitMCLivingEntity)shooter).asLivingEntity());
	}

    public Projectile asProjectile() {
        return proj;
    }
}
