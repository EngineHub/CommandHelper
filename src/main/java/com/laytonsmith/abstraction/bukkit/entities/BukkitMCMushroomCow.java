package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCMushroomCow;
import com.laytonsmith.abstraction.enums.MCMushroomCowType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.MushroomCow;

public class BukkitMCMushroomCow extends BukkitMCLivingEntity implements MCMushroomCow {

	MushroomCow c;

	public BukkitMCMushroomCow(Entity cow) {
		super(cow);
		this.c = (MushroomCow) cow;
	}

	@Override
	public MushroomCow getHandle() {
		return c;
	}

	@Override
	public MCMushroomCowType getVariant() {
		return MCMushroomCowType.valueOf(c.getVariant().name());
	}

	@Override
	public void setVariant(MCMushroomCowType type) {
		c.setVariant(MushroomCow.Variant.valueOf(type.name()));
	}
}
