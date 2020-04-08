package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCPhantom;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Phantom;

public class BukkitMCPhantom extends BukkitMCLivingEntity implements MCPhantom {

	Phantom p;

	public BukkitMCPhantom(Entity be) {
		super(be);
		p = (Phantom) be;
	}

	@Override
	public int getSize() {
		return p.getSize();
	}

	@Override
	public void setSize(int size) {
		p.setSize(size);
	}
}
