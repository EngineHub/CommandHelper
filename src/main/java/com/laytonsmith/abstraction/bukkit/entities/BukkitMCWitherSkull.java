package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.WitherSkull;

import com.laytonsmith.abstraction.bukkit.BukkitMCFireball;
import com.laytonsmith.abstraction.entities.MCWitherSkull;

/**
 *
 * @author Veyyn
 */
public class BukkitMCWitherSkull extends BukkitMCFireball implements MCWitherSkull {

	private final WitherSkull _skull;

	public BukkitMCWitherSkull(WitherSkull skull) {
		super(skull);
		_skull = skull;
	}

	@Override
	public WitherSkull getHandle() {
		return _skull;
	}

	@Override
	public boolean isCharged() {
		return _skull.isCharged();
	}

	@Override
	public void setCharged(boolean charged) {
		_skull.setCharged(charged);
	}
}