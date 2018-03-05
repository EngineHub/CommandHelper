package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCEnderDragon;
import com.laytonsmith.abstraction.enums.MCEnderDragonPhase;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCEnderDragonPhase;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;

public class BukkitMCEnderDragon extends BukkitMCComplexLivingEntity implements MCEnderDragon {

	EnderDragon ed;

	public BukkitMCEnderDragon(Entity ent) {
		super(ent);
		ed = (EnderDragon) ent;
	}

	public BukkitMCEnderDragon(AbstractionObject ao) {
		this((EnderDragon) ao.getHandle());
	}

	@Override
	public EnderDragon getHandle() {
		return ed;
	}

	@Override
	public MCEnderDragonPhase getPhase() {
		return MCEnderDragonPhase.valueOf(ed.getPhase().name());
	}

	@Override
	public void setPhase(MCEnderDragonPhase phase) {
		try {
			ed.setPhase(BukkitMCEnderDragonPhase.getConvertor().getConcreteEnum(phase));
		} catch(NoSuchMethodError ex) {
			// probably prior to 1.9.2
		}
	}
}
