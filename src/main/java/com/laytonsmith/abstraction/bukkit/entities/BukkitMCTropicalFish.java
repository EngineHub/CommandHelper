package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.entities.MCTropicalFish;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDyeColor;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TropicalFish;

public class BukkitMCTropicalFish extends BukkitMCLivingEntity implements MCTropicalFish {

	TropicalFish entity;

	public BukkitMCTropicalFish(Entity be) {
		super(be);
		this.entity = (TropicalFish) be;
	}

	@Override
	public MCDyeColor getPatternColor() {
		return BukkitMCDyeColor.getConvertor().getAbstractedEnum(entity.getPatternColor());
	}

	@Override
	public void setPatternColor(MCDyeColor color) {
		entity.setPatternColor(BukkitMCDyeColor.getConvertor().getConcreteEnum(color));
	}

	@Override
	public MCDyeColor getBodyColor() {
		return BukkitMCDyeColor.getConvertor().getAbstractedEnum(entity.getBodyColor());
	}

	@Override
	public void setBodyColor(MCDyeColor color) {
		entity.setBodyColor(BukkitMCDyeColor.getConvertor().getConcreteEnum(color));
	}

	@Override
	public MCPattern getPattern() {
		return BukkitMCPattern.getConvertor().getAbstractedEnum(entity.getPattern());
	}

	@Override
	public void setPattern(MCPattern pattern) {
		entity.setPattern(BukkitMCPattern.getConvertor().getConcreteEnum(pattern));
	}

	@abstractionenum(
			implementation = Implementation.Type.BUKKIT,
			forAbstractEnum = MCPattern.class,
			forConcreteEnum = TropicalFish.Pattern.class
	)
	public static class BukkitMCPattern extends EnumConvertor<MCPattern, TropicalFish.Pattern> {

		private static BukkitMCPattern instance;

		public static BukkitMCPattern getConvertor() {
			if(instance == null) {
				instance = new BukkitMCPattern();
			}
			return instance;
		}
	}
}
