package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils.ReflectionException;
import com.laytonsmith.abstraction.entities.MCCat;
import com.laytonsmith.abstraction.enums.MCCatType;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import org.bukkit.DyeColor;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;

import java.util.Locale;

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
		// changed from enum to interface in 1.21
		try {
			NamespacedKey key = ReflectionUtils.invokeMethod(Keyed.class, c.getCatType(), "getKey");
			return MCCatType.valueOf(key.getKey().toUpperCase(Locale.ROOT));
		} catch(ReflectionException ex) {
			// probably before 1.20.4
			return MCCatType.valueOf(ReflectionUtils.invokeMethod(Enum.class, c.getCatType(), "name"));
		}
	}

	@Override
	public void setCatType(MCCatType type) {
		try {
			Cat.Type t = Registry.CAT_VARIANT.get(NamespacedKey.minecraft(type.name().toLowerCase(Locale.ROOT)));
			if(t == null) {
				return;
			}
			c.setCatType(t);
		} catch(NoSuchFieldError ex) {
			// probably before 1.20.4
			try {
				Class cls = Class.forName("org.bukkit.entity.Cat$Type");
				c.setCatType(ReflectionUtils.invokeMethod(cls, null, "valueOf",
						new Class[]{String.class}, new Object[]{type.name()}));
			} catch (ClassNotFoundException exc) {
				throw new RuntimeException(exc);
			}
		}
	}
}
