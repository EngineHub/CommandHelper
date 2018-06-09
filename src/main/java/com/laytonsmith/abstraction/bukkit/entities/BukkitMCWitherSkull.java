package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCWitherSkull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.WitherSkull;

public class BukkitMCWitherSkull extends BukkitMCFireball implements MCWitherSkull {

	private final WitherSkull skull;

	public BukkitMCWitherSkull(Entity skull) {
		super(skull);
		this.skull = (WitherSkull) skull;
	}

	@Override
	public WitherSkull getHandle() {
		return this.skull;
	}

	@Override
	public boolean isCharged() {
		return this.skull.isCharged();
	}

	@Override
	public void setCharged(boolean charged) {
		this.skull.setCharged(charged);
	}
}
