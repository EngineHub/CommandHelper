package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.LivingEntity;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCUnknownLivingEntity;

/**
 *
 * @author Hekta
 */
public class BukkitMCUnknownLivingEntity extends BukkitMCLivingEntity implements MCUnknownLivingEntity {

	public BukkitMCUnknownLivingEntity(LivingEntity unknownEntity) {
		super(unknownEntity);
	}

	public BukkitMCUnknownLivingEntity(AbstractionObject ao) {
		this((LivingEntity) ao.getHandle());
	}
}