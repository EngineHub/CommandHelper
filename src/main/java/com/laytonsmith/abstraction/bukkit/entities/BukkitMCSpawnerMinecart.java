package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.minecart.SpawnerMinecart;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCSpawnerMinecart;

/**
 * 
 * @author Hekta
 */
public class BukkitMCSpawnerMinecart extends BukkitMCMinecart implements MCSpawnerMinecart {

	public BukkitMCSpawnerMinecart(SpawnerMinecart minecart) {
		super(minecart);
	}

	public BukkitMCSpawnerMinecart(AbstractionObject ao) {
		this((SpawnerMinecart) ao.getHandle());
	}

	@Override
	public SpawnerMinecart getHandle() {
		return (SpawnerMinecart) metadatable;
	}
}