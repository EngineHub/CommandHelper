package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.entities.MCHorse;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;

public class BukkitMCHorse extends BukkitMCAbstractHorse implements MCHorse {

	Horse h;

	public BukkitMCHorse(Entity t) {
		super(t);
		this.h = (Horse) t;
	}

	@Override
	public MCHorseColor getColor() {
		return BukkitMCHorseColor.getConvertor().getAbstractedEnum(h.getColor());
	}

	@Override
	public MCHorsePattern getPattern() {
		return BukkitMCHorsePattern.getConvertor().getAbstractedEnum(h.getStyle());
	}

	@Override
	public void setColor(MCHorseColor color) {
		h.setColor(BukkitMCHorseColor.getConvertor().getConcreteEnum(color));
	}

	@Override
	public void setPattern(MCHorsePattern pattern) {
		h.setStyle(BukkitMCHorsePattern.getConvertor().getConcreteEnum(pattern));
	}

	@Override
	public void setArmor(MCItemStack stack) {
		h.getInventory().setArmor(((BukkitMCItemStack) stack).asItemStack());
	}

	@Override
	public MCItemStack getArmor() {
		return new BukkitMCItemStack(h.getInventory().getArmor());
	}

	@abstractionenum(
			implementation = Implementation.Type.BUKKIT,
			forAbstractEnum = MCHorseColor.class,
			forConcreteEnum = Horse.Color.class
	)
	public static class BukkitMCHorseColor extends EnumConvertor<MCHorseColor, Horse.Color> {

		private static BukkitMCHorseColor instance;

		public static BukkitMCHorseColor getConvertor() {
			if(instance == null) {
				instance = new BukkitMCHorseColor();
			}
			return instance;
		}
	}

	@abstractionenum(
			implementation = Implementation.Type.BUKKIT,
			forAbstractEnum = MCHorsePattern.class,
			forConcreteEnum = Horse.Style.class
	)
	public static class BukkitMCHorsePattern extends EnumConvertor<MCHorsePattern, Horse.Style> {

		private static BukkitMCHorsePattern instance;

		public static BukkitMCHorsePattern getConvertor() {
			if(instance == null) {
				instance = new BukkitMCHorsePattern();
			}
			return instance;
		}

		@Override
		protected MCHorsePattern getAbstractedEnumCustom(Horse.Style concrete) {
			switch(concrete) {
				case WHITE:
					return MCHorsePattern.SOCKS;
			}
			return super.getAbstractedEnumCustom(concrete);
		}

		@Override
		protected Horse.Style getConcreteEnumCustom(MCHorsePattern abstracted) {
			switch(abstracted) {
				case SOCKS:
					return Horse.Style.WHITE;
			}
			return super.getConcreteEnumCustom(abstracted);
		}
	}
}
