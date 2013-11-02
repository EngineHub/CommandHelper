package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Squid;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCSquid;

/**
 *
 * @author Hekta
 */
public class BukkitMCSquid extends BukkitMCCreature implements MCSquid {

	public BukkitMCSquid(Squid squid) {
		super(squid);
	}

	public BukkitMCSquid(AbstractionObject ao) {
		this((Squid) ao.getHandle());
	}

	@Override
	public Squid getHandle() {
		return (Squid) metadatable;
	}
}