package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCParrot;
import com.laytonsmith.abstraction.enums.MCParrotType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Parrot;

public class BukkitMCParrot extends BukkitMCTameable implements MCParrot {

	Parrot p;

	public BukkitMCParrot(Entity be) {
		super(be);
		this.p = (Parrot) be;
	}

	@Override
	public boolean isSitting() {
		return p.isSitting();
	}

	@Override
	public void setSitting(boolean sitting) {
		p.setSitting(sitting);
	}

	@Override
	public MCParrotType getVariant() {
		return MCParrotType.valueOf(p.getVariant().name());
	}

	@Override
	public void setVariant(MCParrotType variant) {
		p.setVariant(Parrot.Variant.valueOf(variant.name()));
	}

}
