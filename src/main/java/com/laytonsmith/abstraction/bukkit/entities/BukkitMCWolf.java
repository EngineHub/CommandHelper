package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.AbstractionUtils;
import com.laytonsmith.abstraction.MCAnimalTamer;
import com.laytonsmith.abstraction.MCTameable;
import com.laytonsmith.abstraction.bukkit.BukkitMCAgeable;
import com.laytonsmith.abstraction.entities.MCWolf;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.DyeColor;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wolf;

/**
 * 
 * @author jb_aero
 */
public class BukkitMCWolf extends BukkitMCAgeable implements MCWolf, MCTameable {
	
	@WrappedItem Wolf w;
	
	public MCDyeColor getCollarColor() {
		return MCDyeColor.valueOf(w.getCollarColor().name());
	}
	public boolean isAngry() {
		return w.isAngry();
	}
	public boolean isSitting() {
		return w.isSitting();
	}
	public void setAngry(boolean angry) {
		w.setAngry(angry);
	}
	public void setSitting(boolean sitting) {
		w.setSitting(sitting);
	}
	public void setCollarColor(MCDyeColor color) {
		w.setCollarColor(DyeColor.valueOf(color.name()));
	}

	public boolean isTamed() {
		return w.isTamed();
	}

	public void setTamed(boolean bln) {
		w.setTamed(bln);
	}

	public MCAnimalTamer getOwner() {
		return AbstractionUtils.wrap(w.getOwner());
	}

	public void setOwner(MCAnimalTamer at) {
		w.setOwner((AnimalTamer)at.getHandle());
	}
	
}
