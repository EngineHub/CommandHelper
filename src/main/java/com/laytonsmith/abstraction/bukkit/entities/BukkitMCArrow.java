package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Arrow;

import com.laytonsmith.abstraction.entities.MCArrow;
import com.laytonsmith.abstraction.bukkit.BukkitMCProjectile;

/**
 *
 * @author Veyyn
 */
public class BukkitMCArrow extends BukkitMCProjectile implements MCArrow {

	private final Arrow m_arrow;

	public BukkitMCArrow(Arrow arrow) {
		super(arrow);
		m_arrow = arrow;
	}

	@Override
	public int getKnockbackStrength() {
		return m_arrow.getKnockbackStrength();
	}

	@Override
	public void setKnockbackStrength(int strength) {
		m_arrow.setKnockbackStrength(strength);
	}

	@Override
	public boolean isCritical() {
		return m_arrow.isCritical();
	}

	@Override
	public void setCritical(boolean critical) {
		m_arrow.setCritical(critical);
	}
}