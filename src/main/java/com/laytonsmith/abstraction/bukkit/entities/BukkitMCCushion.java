package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCCushion;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Cushion;

public class BukkitMCCushion extends BukkitMCEntity implements MCCushion {

	Cushion c;

	public BukkitMCCushion(Entity e) {
		super(e);
		this.c = (Cushion) e;
	}

	@Override
	public MCDyeColor getColor() {
		try {
			return BukkitMCDyeColor.getConvertor().getAbstractedEnum(c.getColor());
		} catch(NoSuchMethodError ex) {
			// may be missing in Spigot
			return MCDyeColor.WHITE;
		}
	}

	@Override
	public void setColor(MCDyeColor color) {
		try {
			c.setColor(BukkitMCDyeColor.getConvertor().getConcreteEnum(color));
		} catch(NoSuchMethodError ignore) {
			// may be missing in Spigot
		}
	}
}
