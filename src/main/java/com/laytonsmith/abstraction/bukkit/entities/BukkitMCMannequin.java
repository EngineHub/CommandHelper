package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCMannequin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;

public class BukkitMCMannequin extends BukkitMCLivingEntity implements MCMannequin {

	private final Mannequin m;

	public BukkitMCMannequin(Entity mannequin) {
		super(mannequin);
		this.m = (Mannequin) mannequin;
	}

	@Override
	public Entity getHandle() {
		return super.getHandle();
	}

	@Override
	public boolean isImmovable() {
		return this.m.isImmovable();
	}

	@Override
	public void setImmovable(boolean immovable) {
		this.m.setImmovable(immovable);
	}
}
