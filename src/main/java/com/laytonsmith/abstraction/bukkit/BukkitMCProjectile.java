/**
 * 
 */
package com.laytonsmith.abstraction.bukkit;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCEntityType;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCProjectile;
import com.laytonsmith.abstraction.bukkit.BukkitMCEntity;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;

/**
 * @author EntityReborn
 *
 */
public class BukkitMCProjectile extends BukkitMCEntity implements MCProjectile  {
	
	Projectile p;
	public BukkitMCProjectile(Projectile proj) {
		super(proj);
		p = proj;
	}

	public boolean doesBounce() {
		return p.doesBounce();
	}

	public MCLivingEntity getShooter() {
		MCEntity e = BukkitConvertor.BukkitGetCorrectEntity(p.getShooter());
		
		if (e instanceof MCLivingEntity) {
			return (MCLivingEntity)e;
		}
		
		return null;
	}

	public void setBounce(boolean arg0) {
		p.setBounce(arg0);
	}

	public void setShooter(MCLivingEntity arg0) {
		p.setShooter((LivingEntity)arg0.getHandle());

	}
}
