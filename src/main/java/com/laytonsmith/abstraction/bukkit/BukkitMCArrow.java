package com.laytonsmith.abstraction.bukkit;

import org.bukkit.entity.Projectile;

import com.laytonsmith.abstraction.MCArrow;

public class BukkitMCArrow extends BukkitMCProjectile implements MCArrow {

	public BukkitMCArrow(Projectile proj) {
		super(proj);
	}
}
