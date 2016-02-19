package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCOcelot;
import com.laytonsmith.abstraction.enums.MCOcelotType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;

public class BukkitMCOcelot extends BukkitMCTameable implements MCOcelot {

	Ocelot o;
	public BukkitMCOcelot(Entity be) {
		super(be);
		this.o = (Ocelot) be;
	}
	
	public BukkitMCOcelot(AbstractionObject ao){
        super((LivingEntity)ao.getHandle());
        this.o = (Ocelot) ao.getHandle();
    }
	
	@Override
	public MCOcelotType getCatType() {
		return MCOcelotType.valueOf(o.getCatType().name());
	}
	@Override
	public boolean isSitting() {
		return o.isSitting();
	}
	@Override
	public void setCatType(MCOcelotType type) {
		o.setCatType(Ocelot.Type.valueOf(type.name()));
	}
	@Override
	public void setSitting(boolean sitting) {
		o.setSitting(sitting);
	}

}
