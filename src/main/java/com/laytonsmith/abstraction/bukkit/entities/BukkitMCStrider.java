package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCStrider;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Strider;

public class BukkitMCStrider extends BukkitMCAgeable implements MCStrider {

	Strider s;

	public BukkitMCStrider(Entity be) {
		super(be);
		s = (Strider) be;
	}
}
