package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCWolf;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import org.bukkit.DyeColor;
import org.bukkit.entity.Wolf;

/**
 * 
 * @author jb_aero
 */
public class BukkitMCWolf extends BukkitMCTameable implements MCWolf {
	
	public BukkitMCWolf(Wolf wolf) {
		super(wolf);
	}
	
	public BukkitMCWolf(AbstractionObject ao) {
        this((Wolf) ao.getHandle());
    }

	@Override
	public Wolf getHandle() {
		return (Wolf) metadatable;
	}
	
	public MCDyeColor getCollarColor() {
		return MCDyeColor.valueOf(getHandle().getCollarColor().name());
	}
	public boolean isAngry() {
		return getHandle().isAngry();
	}
	public boolean isSitting() {
		return getHandle().isSitting();
	}
	public void setAngry(boolean angry) {
		getHandle().setAngry(angry);
	}
	public void setSitting(boolean sitting) {
		getHandle().setSitting(sitting);
	}
	public void setCollarColor(MCDyeColor color) {
		getHandle().setCollarColor(DyeColor.valueOf(color.name()));
	}
}