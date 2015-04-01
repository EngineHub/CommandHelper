package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCSheep;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Sheep;

public class BukkitMCSheep extends BukkitMCAgeable implements MCSheep {

	Sheep s;

	public BukkitMCSheep(Entity be) {
		super(be);
		this.s = (Sheep) be;
	}

	@Override
	public MCDyeColor getColor() {
		return BukkitMCDyeColor.getConvertor().getAbstractedEnum(s.getColor());
	}

	@Override
	public void setColor(MCDyeColor color) {
		s.setColor(BukkitMCDyeColor.getConvertor().getConcreteEnum(color));
	}

	@Override
	public boolean isSheared() {
		return s.isSheared();
	}

	@Override
	public void setSheared(boolean shear) {
		s.setSheared(shear);
	}
}
