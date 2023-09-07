package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCColorableArmorMeta;
import org.bukkit.inventory.meta.ColorableArmorMeta;

public class BukkitMCColorableArmorMeta extends BukkitMCArmorMeta implements MCColorableArmorMeta {

	ColorableArmorMeta lam;

	public BukkitMCColorableArmorMeta(ColorableArmorMeta im) {
		super(im);
		lam = im;
	}

	@Override
	public MCColor getColor() {
		return BukkitMCColor.GetMCColor(lam.getColor());
	}

	@Override
	public void setColor(MCColor color) {
		lam.setColor(BukkitMCColor.GetColor(color));
	}

}
