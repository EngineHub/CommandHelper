package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.bukkit.BukkitMCAgeable;
import com.laytonsmith.abstraction.entities.MCSheep;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDyeColor;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.entity.Sheep;

public class BukkitMCSheep extends BukkitMCAgeable implements MCSheep {

	@WrappedItem Sheep s;
	public BukkitMCSheep(Sheep be) {
		super(be);
		this.s = be;
	}
	
	public BukkitMCSheep(AbstractionObject a){
		super(a);
		this.s = a.getHandle();
	}

	@Override
	public Sheep getHandle() {
		return s;
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
