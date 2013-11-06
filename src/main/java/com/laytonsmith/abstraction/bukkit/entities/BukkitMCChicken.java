package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Chicken;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCChicken;

/**
 *
 * @author Hekta
 */
public class BukkitMCChicken extends BukkitMCAgeable implements MCChicken {

	public BukkitMCChicken(Chicken chicken) {
		super(chicken);
	}

	public BukkitMCChicken(AbstractionObject ao) {
		this((Chicken) ao.getHandle());
	}

	@Override
	public Chicken getHandle() {
		return (Chicken) metadatable;
	}
}