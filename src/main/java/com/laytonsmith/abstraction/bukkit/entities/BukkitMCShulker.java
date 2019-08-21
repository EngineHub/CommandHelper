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
		return BukkitMCDyeColor.getConvertor().getAbstractedEnum(sh.getColor());
	}

	@Override
	public void setColor(MCDyeColor color) {
		sh.setColor(BukkitMCDyeColor.getConvertor().getConcreteEnum(color));
	}
}
