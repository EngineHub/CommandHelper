package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCCamel;
import org.bukkit.entity.Camel;
import org.bukkit.entity.Entity;

public class BukkitMCCamel extends BukkitMCAbstractHorse implements MCCamel {

	Camel c;

	public BukkitMCCamel(Entity t) {
		super(t);
		c = (Camel) t;
	}

	@Override
	public boolean isDashing() {
		return c.isDashing();
	}

	@Override
	public void setDashing(boolean dashing) {
		c.setDashing(dashing);
	}
}
