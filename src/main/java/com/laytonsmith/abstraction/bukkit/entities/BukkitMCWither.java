package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Wither;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCWither;

/**
 *
 * @author Hekta
 */
public class BukkitMCWither extends BukkitMCCreature implements MCWither {

	public BukkitMCWither(Wither wither) {
		super(wither);
	}

	public BukkitMCWither(AbstractionObject ao) {
		this((Wither) ao.getHandle());
	}

	@Override
	public Wither getHandle() {
		return (Wither) metadatable;
	}
}