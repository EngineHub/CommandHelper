package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionUtils;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCProjectile;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.entity.Projectile;

public class BukkitMCProjectile extends BukkitMCEntity implements MCProjectile {
	
	@WrappedItem Projectile proj;
	
	public boolean doesBounce() {
		return proj.doesBounce();
	}

	public MCLivingEntity getShooter() {
		MCEntity e = AbstractionUtils.wrap(proj.getShooter());
		
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
