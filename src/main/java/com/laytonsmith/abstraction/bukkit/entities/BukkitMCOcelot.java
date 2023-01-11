package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCOcelot;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;

public class BukkitMCOcelot extends BukkitMCAnimal implements MCOcelot {

	Ocelot o;

	public BukkitMCOcelot(Entity be) {
		super(be);
		this.o = (Ocelot) be;
	}

	public BukkitMCOcelot(AbstractionObject ao) {
		super((LivingEntity) ao.getHandle());
		this.o = (Ocelot) ao.getHandle();
	}

}
