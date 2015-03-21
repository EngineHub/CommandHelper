package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.entities.MCHorse;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;

/**
 * 
 * @author jb_aero
 */
public class BukkitMCHorse extends BukkitMCTameable implements MCHorse {

	Horse h;

	public BukkitMCHorse(Entity t) {
		super(t);
		this.h = (Horse) t;
	}
	
	@Override
	public MCInventory getInventory() {
		return new BukkitMCInventory(h.getInventory());
	}
	
	@Override
	public MCHorseVariant getVariant() {
		return BukkitMCHorseVariant.getConvertor().getAbstractedEnum(h.getVariant());
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
	public void setVariant(MCHorseVariant variant) {
		h.setVariant(BukkitMCHorseVariant.getConvertor().getConcreteEnum(variant));
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
	public double getJumpStrength() {
		return h.getJumpStrength();
	}
	
	@Override
	public void setJumpStrength(double strength) {
		h.setJumpStrength(strength);
	}
	@Override
	public boolean hasChest() {
		return h.isCarryingChest();
	}
	
	@Override
	public void setHasChest(boolean hasChest) {
		h.setCarryingChest(hasChest);
	}
	
	@Override
	public int getDomestication() {
		return h.getDomestication();
	}
	
	@Override
	public int getMaxDomestication() {
		return h.getMaxDomestication();
	}
	
	@Override
	public void setDomestication(int level) {
		h.setDomestication(level);
	}
	
	@Override
	public void setMaxDomestication(int level) {
		h.setMaxDomestication(level);
	}

	@Override
	public void setSaddle(MCItemStack stack) {
		h.getInventory().setSaddle(((BukkitMCItemStack)stack).asItemStack());
	}

	@Override
	public MCItemStack getSaddle() {
		return new BukkitMCItemStack(h.getInventory().getSaddle());
	}

	@Override
	public void setArmor(MCItemStack stack) {
		h.getInventory().setArmor(((BukkitMCItemStack)stack).asItemStack());
	}

	@Override
	public MCItemStack getArmor() {
		return new BukkitMCItemStack(h.getInventory().getArmor());
	}
	
	@abstractionenum(
			implementation= Implementation.Type.BUKKIT,
			forAbstractEnum=MCHorseVariant.class,
			forConcreteEnum=Horse.Variant.class
	)
	public static class BukkitMCHorseVariant extends EnumConvertor<MCHorseVariant, Horse.Variant>{

		private static BukkitMCHorseVariant instance;
		
		public static BukkitMCHorseVariant getConvertor() {
			if (instance == null) {
				instance = new BukkitMCHorseVariant();
			}
			return instance;
		}
		
		@Override
		protected MCHorseVariant getAbstractedEnumCustom(Horse.Variant concrete) {
			switch (concrete) {
				case SKELETON_HORSE:
					return MCHorseVariant.SKELETON;
				case UNDEAD_HORSE:
					return MCHorseVariant.ZOMBIE;
			}
			return super.getAbstractedEnumCustom(concrete);
		}
		
		@Override
		protected Horse.Variant getConcreteEnumCustom(MCHorseVariant abstracted) {
			switch (abstracted) {
				case SKELETON:
					return Horse.Variant.SKELETON_HORSE;
				case ZOMBIE:
					return Horse.Variant.UNDEAD_HORSE;
			}
			return super.getConcreteEnumCustom(abstracted);
		}
	}
	
	@abstractionenum(
			implementation= Implementation.Type.BUKKIT,
			forAbstractEnum=MCHorseColor.class,
			forConcreteEnum=Horse.Color.class
	)
	public static class BukkitMCHorseColor extends EnumConvertor<MCHorseColor, Horse.Color>{

		private static BukkitMCHorseColor instance;
		
		public static BukkitMCHorseColor getConvertor() {
			if (instance == null) {
				instance = new BukkitMCHorseColor();
			}
			return instance;
		}
	}
	
	@abstractionenum(
			implementation= Implementation.Type.BUKKIT,
			forAbstractEnum=MCHorsePattern.class,
			forConcreteEnum=Horse.Style.class
	)
	public static class BukkitMCHorsePattern extends EnumConvertor<MCHorsePattern, Horse.Style>{

		private static BukkitMCHorsePattern instance;
		
		public static BukkitMCHorsePattern getConvertor() {
			if (instance == null) {
				instance = new BukkitMCHorsePattern();
			}
			return instance;
		}
		
		@Override
		protected MCHorsePattern getAbstractedEnumCustom(Horse.Style concrete) {
			switch (concrete) {
				case WHITE:
					return MCHorsePattern.SOCKS;
			}
			return super.getAbstractedEnumCustom(concrete);
		}
		
		@Override
		protected Horse.Style getConcreteEnumCustom(MCHorsePattern abstracted) {
			switch (abstracted) {
				case SOCKS:
					return Horse.Style.WHITE;
			}
			return super.getConcreteEnumCustom(abstracted);
		}
	}
}
