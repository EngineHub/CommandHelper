package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.entities.MCEntity;
import com.laytonsmith.abstraction.entities.MCLivingEntity;
import com.laytonsmith.abstraction.entities.MCProjectile;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;

public abstract class BukkitMCProjectile extends BukkitMCEntity implements MCProjectile {

	public BukkitMCProjectile(Projectile projectile) {
		super(projectile);
	}

	@Override
	public Projectile getHandle() {
		return (Projectile) metadatable;
	}

	public boolean doesBounce() {
		return getHandle().doesBounce();
	}

	public MCLivingEntity getShooter() {
		MCEntity shooter = BukkitConvertor.BukkitGetCorrectEntity(getHandle().getShooter());
		if (shooter != null) {
			return (MCLivingEntity) shooter;
		} else {
			return null;
		}
	}

	public void setBounce(boolean doesBounce) {
		getHandle().setBounce(doesBounce);
	}

	public void setShooter(MCLivingEntity shooter) {
		getHandle().setShooter((LivingEntity) shooter.getHandle());
	}
}