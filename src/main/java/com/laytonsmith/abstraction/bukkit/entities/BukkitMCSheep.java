package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Sheep;

import com.laytonsmith.abstraction.bukkit.BukkitMCAgeable;
import com.laytonsmith.abstraction.entities.MCSheep;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDyeColor;

public class BukkitMCSheep extends BukkitMCAgeable implements MCSheep {

	Sheep s;
	public BukkitMCSheep(Sheep be) {
		super(be);
		this.s = be;
	}

	public MCDyeColor getColor() {
		return BukkitMCDyeColor.getConvertor().getAbstractedEnum(s.getColor());
	}

	public void setColor(MCDyeColor color) {
		s.setColor(BukkitMCDyeColor.getConvertor().getConcreteEnum(color));
	}

	public boolean isSheared() {
		return s.isSheared();
	}

	public void setSheared(boolean shear) {
		s.setSheared(shear);
	}
}
