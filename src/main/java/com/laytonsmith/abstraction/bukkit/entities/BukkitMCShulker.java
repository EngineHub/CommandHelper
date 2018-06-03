package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCShulker;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Shulker;

public class BukkitMCShulker extends BukkitMCLivingEntity implements MCShulker {

	private Shulker sh;

	public BukkitMCShulker(Entity be) {
		super(be);
		this.sh = (Shulker) be;
	}

	@Override
	public MCDyeColor getColor() {
		try {
			return BukkitMCDyeColor.getConvertor().getAbstractedEnum(sh.getColor());
		} catch(NoSuchMethodError ex) {
			// probably prior to 1.12
		}
		return MCDyeColor.PURPLE;
	}

	@Override
	public void setColor(MCDyeColor color) {
		try {
			sh.setColor(BukkitMCDyeColor.getConvertor().getConcreteEnum(color));
		} catch(NoSuchMethodError ex) {
			// probably prior to 1.12
		}
	}
}
