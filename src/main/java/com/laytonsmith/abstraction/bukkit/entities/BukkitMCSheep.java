package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Sheep;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCSheep;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDyeColor;

public class BukkitMCSheep extends BukkitMCAgeable implements MCSheep {

	public BukkitMCSheep(Sheep sheep) {
		super(sheep);
	}

	public BukkitMCSheep(AbstractionObject ao) {
		this((Sheep) ao.getHandle());
	}

	@Override
	public Sheep getHandle() {
		return (Sheep) metadatable;
	}

	public MCDyeColor getColor() {
		return BukkitMCDyeColor.getConvertor().getAbstractedEnum(getHandle().getColor());
	}

	public void setColor(MCDyeColor color) {
		getHandle().setColor(BukkitMCDyeColor.getConvertor().getConcreteEnum(color));
	}

	public boolean isSheared() {
		return getHandle().isSheared();
	}

	public void setSheared(boolean shear) {
		getHandle().setSheared(shear);
	}
}
