package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionUtils;
import com.laytonsmith.abstraction.MCAnimalTamer;
import com.laytonsmith.abstraction.MCTameable;
import com.laytonsmith.abstraction.bukkit.BukkitMCAgeable;
import com.laytonsmith.abstraction.entities.MCOcelot;
import com.laytonsmith.abstraction.enums.MCOcelotType;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Ocelot;

public class BukkitMCOcelot extends BukkitMCAgeable implements MCOcelot, MCTameable {

	@WrappedItem Ocelot o;
	
	public MCOcelotType getCatType() {
		return MCOcelotType.valueOf(o.getCatType().name());
	}
	public boolean isSitting() {
		return o.isSitting();
	}
	public void setCatType(MCOcelotType type) {
		o.setCatType(Ocelot.Type.valueOf(type.name()));
	}
	public void setSitting(boolean sitting) {
		o.setSitting(sitting);
	}

	public boolean isTamed() {
		return o.isTamed();
	}

	public void setTamed(boolean bln) {
		o.setTamed(bln);
	}

	public MCAnimalTamer getOwner() {
		return AbstractionUtils.wrap(o.getOwner());
	}

	public void setOwner(MCAnimalTamer at) {
		o.setOwner((AnimalTamer)at.getHandle());
	}

}
