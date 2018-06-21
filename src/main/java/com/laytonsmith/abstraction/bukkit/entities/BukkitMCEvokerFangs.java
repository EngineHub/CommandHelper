package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.entities.MCEvokerFangs;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;

public class BukkitMCEvokerFangs extends BukkitMCEntity implements MCEvokerFangs {

	private EvokerFangs ef;

	public BukkitMCEvokerFangs(Entity ent) {
		super(ent);
		ef = (EvokerFangs) ent;
	}

	@Override
	public MCLivingEntity getOwner() {
		if(ef.getOwner() == null) {
			return null;
		}
		return (MCLivingEntity) BukkitConvertor.BukkitGetCorrectEntity(ef.getOwner());
	}

	@Override
	public void setOwner(MCLivingEntity owner) {
		if(owner == null) {
			ef.setOwner(null);
		} else {
			ef.setOwner((LivingEntity) owner.getHandle());
		}
	}
}
