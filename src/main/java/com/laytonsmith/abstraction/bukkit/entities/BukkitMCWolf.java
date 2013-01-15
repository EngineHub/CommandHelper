package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wolf;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.bukkit.BukkitMCTameable;
import com.laytonsmith.abstraction.entities.MCWolf;
import com.laytonsmith.abstraction.enums.MCDyeColor;

/**
 * 
 * @author jb_aero
 */
public class BukkitMCWolf extends BukkitMCTameable implements MCWolf {
	
	Wolf w;
	public BukkitMCWolf(Entity be) {
		super(be);
		this.w = (Wolf) be;
	}
	
	public BukkitMCWolf(AbstractionObject ao){
        super((LivingEntity)ao.getHandle());
        this.w = (Wolf) ao.getHandle();
    }
	
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
	
}
