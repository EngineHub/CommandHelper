package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCStrider;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Strider;

public class BukkitMCStrider extends BukkitMCAgeable implements MCStrider {

	private final Strider s;

	public BukkitMCStrider(Entity be) {
		super(be);
		s = (Strider) be;
	}

	@Override
	public boolean isSaddled() {
		return s.hasSaddle();
	}

	@Override
	public void setSaddled(boolean saddled) {
		s.setSaddle(saddled);
	}
}
