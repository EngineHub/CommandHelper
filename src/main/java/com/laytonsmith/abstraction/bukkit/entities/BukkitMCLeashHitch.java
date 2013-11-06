package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.LeashHitch;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCLeashHitch;

/**
 *
 * @author Hekta
 */
public class BukkitMCLeashHitch extends BukkitMCHanging implements MCLeashHitch {

	public BukkitMCLeashHitch(LeashHitch leash) {
		super(leash);
	}

	public BukkitMCLeashHitch(AbstractionObject ao) {
		this((LeashHitch) ao.getHandle());
	}

	@Override
	public LeashHitch getHandle() {
		return (LeashHitch) metadatable;
	}
}