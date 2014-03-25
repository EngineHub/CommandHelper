package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.WitherSkull;

import com.laytonsmith.abstraction.bukkit.BukkitMCFireball;
import com.laytonsmith.abstraction.entities.MCWitherSkull;

/**
 *
 * @author Veyyn
 */
public class BukkitMCWitherSkull extends BukkitMCFireball implements MCWitherSkull {

	private final WitherSkull m_skull;

	public BukkitMCWitherSkull(WitherSkull skull) {
		super(skull);
		m_skull = skull;
	}

	@Override
	public WitherSkull getHandle() {
		return m_skull;
	}

	@Override
	public boolean isCharged() {
		return m_skull.isCharged();
	}

	@Override
	public void setCharged(boolean charged) {
		m_skull.setCharged(charged);
	}
}