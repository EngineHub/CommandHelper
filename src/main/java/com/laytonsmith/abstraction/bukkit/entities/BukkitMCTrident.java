package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCTrident;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Trident;

public class BukkitMCTrident extends BukkitMCArrow implements MCTrident {

	private final Trident trident;

	public BukkitMCTrident(Entity trident) {
		super(trident);
		this.trident = (Trident) trident;
	}
}
