package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCCat;
import com.laytonsmith.abstraction.enums.MCCatType;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import org.bukkit.DyeColor;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;

public class BukkitMCCat extends BukkitMCTameable implements MCCat {

	Cat c;

	public BukkitMCCat(Entity be) {
		super(be);
		this.c = (Cat) be;
	}

	@Override
	public MCDyeColor getCollarColor() {
		return MCDyeColor.valueOf(c.getCollarColor().name());
	}

	@Override
	public void setCollarColor(MCDyeColor color) {
		c.setCollarColor(DyeColor.valueOf(color.name()));
	}

	@Override
	public boolean isSitting() {
		return c.isSitting();
	}

	@Override
	public void setSitting(boolean sitting) {
		c.setSitting(sitting);
	}

	@Override
	public MCCatType getCatType() {
		return MCCatType.valueOf(c.getCatType().name());
	}

	@Override
	public void setCatType(MCCatType type) {
		c.setCatType(Cat.Type.valueOf(type.name()));
	}
}
