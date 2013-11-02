package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.minecart.StorageMinecart;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCStorageMinecart;

/**
 * 
 * @author Hekta
 */
public class BukkitMCStorageMinecart extends BukkitMCMinecart implements MCStorageMinecart {

	public BukkitMCStorageMinecart(StorageMinecart minecart) {
		super(minecart);
	}

	/**
	 * Will be deprecated.
	 */
	public BukkitMCStorageMinecart(org.bukkit.entity.StorageMinecart minecart) {
		super(minecart);
	}

	public BukkitMCStorageMinecart(AbstractionObject ao) {
		this((StorageMinecart) ao.getHandle());
	}

	@Override
	public StorageMinecart getHandle() {
		return (StorageMinecart) metadatable;
	}
}