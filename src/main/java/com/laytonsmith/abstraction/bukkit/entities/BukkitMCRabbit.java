package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCRabbit;
import com.laytonsmith.abstraction.enums.MCRabbitType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Rabbit;

public class BukkitMCRabbit extends BukkitMCAgeable implements MCRabbit {

	Rabbit r;
	public BukkitMCRabbit(Entity be) {
		super(be);
		this.r = (Rabbit) be;
	}

	public BukkitMCRabbit(AbstractionObject ao){
        super((LivingEntity)ao.getHandle());
        this.r = (Rabbit) ao.getHandle();
    }
	
	@Override
	public MCRabbitType getRabbitType() {
		return MCRabbitType.valueOf(r.getRabbitType().name());
	}
	@Override
	public void setRabbitType(MCRabbitType type) {
		r.setRabbitType(Rabbit.Type.valueOf(type.name()));
	}
}
