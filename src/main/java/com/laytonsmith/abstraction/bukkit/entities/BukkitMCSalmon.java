package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCSalmon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Salmon;

public class BukkitMCSalmon extends BukkitMCLivingEntity implements MCSalmon {

	Salmon entity;

	public BukkitMCSalmon(Entity be) {
		super(be);
		this.entity = (Salmon) be;
	}

	@Override
	public Variant getVariant() {
		return Variant.valueOf(this.entity.getVariant().name());
	}

	@Override
	public void setVariant(Variant size) {
		this.entity.setVariant(Salmon.Variant.valueOf(size.name()));
	}
}
