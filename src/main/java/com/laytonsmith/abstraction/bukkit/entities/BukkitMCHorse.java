package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Horse;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;
import com.laytonsmith.abstraction.entities.MCHorse;
import com.laytonsmith.abstraction.enums.MCHorseColor;
import com.laytonsmith.abstraction.enums.MCHorseStyle;
import com.laytonsmith.abstraction.enums.MCHorseVariant;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCHorseColor;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCHorseStyle;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCHorseVariant;

/**
 * 
 * @author jb_aero
 */
public class BukkitMCHorse extends BukkitMCTameable implements MCHorse {

	public BukkitMCHorse(Horse horse) {
		super(horse);
	}

	public BukkitMCHorse(AbstractionObject ao) {
		this((Horse) ao.getHandle());
	}

	@Override
	public Horse getHandle() {
		return (Horse) metadatable;
	}

	@Override
	public MCInventory getInventory() {
		return new BukkitMCInventory(getHandle().getInventory());
	}

	@Override
	public MCHorseVariant getVariant() {
		return BukkitMCHorseVariant.getConvertor().getAbstractedEnum(getHandle().getVariant());
	}

	@Override
	public MCHorseColor getColor() {
		return BukkitMCHorseColor.getConvertor().getAbstractedEnum(getHandle().getColor());
	}

	@Override
	public MCHorseStyle getStyle() {
		return BukkitMCHorseStyle.getConvertor().getAbstractedEnum(getHandle().getStyle());
	}

	@Override
	public void setVariant(MCHorseVariant variant) {
		getHandle().setVariant(BukkitMCHorseVariant.getConvertor().getConcreteEnum(variant));
	}

	@Override
	public void setColor(MCHorseColor color) {
		getHandle().setColor(BukkitMCHorseColor.getConvertor().getConcreteEnum(color));
	}

	@Override
	public void setStyle(MCHorseStyle style) {
		getHandle().setStyle(BukkitMCHorseStyle.getConvertor().getConcreteEnum(style));
	}

	@Override
	public double getJumpStrength() {
		return getHandle().getJumpStrength();
	}

	@Override
	public void setJumpStrength(double strength) {
		getHandle().setJumpStrength(strength);
	}

	@Override
	public boolean hasChest() {
		return getHandle().isCarryingChest();
	}

	@Override
	public void setHasChest(boolean hasChest) {
		getHandle().setCarryingChest(hasChest);
	}

	@Override
	public int getDomestication() {
		return getHandle().getDomestication();
	}

	@Override
	public int getMaxDomestication() {
		return getHandle().getMaxDomestication();
	}

	@Override
	public void setDomestication(int level) {
		getHandle().setDomestication(level);
	}

	@Override
	public void setMaxDomestication(int level) {
		getHandle().setMaxDomestication(level);
	}
}