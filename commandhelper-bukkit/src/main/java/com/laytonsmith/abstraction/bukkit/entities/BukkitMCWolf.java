package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCWolf;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wolf;

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
	
	@Override
	public MCDyeColor getCollarColor() {
		return MCDyeColor.valueOf(w.getCollarColor().name());
	}
	@Override
	public boolean isAngry() {
		return w.isAngry();
	}
	@Override
	public boolean isSitting() {
		return w.isSitting();
	}
	@Override
	public void setAngry(boolean angry) {
		w.setAngry(angry);
	}
	@Override
	public void setSitting(boolean sitting) {
		w.setSitting(sitting);
	}
	@Override
	public void setCollarColor(MCDyeColor color) {
		w.setCollarColor(DyeColor.valueOf(color.name()));
	}
	
}
