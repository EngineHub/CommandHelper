package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Entity;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCUnknownEntity;

/**
 *
 * @author Hekta
 */
public class BukkitMCUnknownEntity extends BukkitMCEntity implements MCUnknownEntity {

	public BukkitMCUnknownEntity(Entity unknownEntity) {
		super(unknownEntity);
	}

	public BukkitMCUnknownEntity(AbstractionObject ao) {
		this((Entity) ao.getHandle());
	}
}