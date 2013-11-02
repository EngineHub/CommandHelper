package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Giant;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCGiant;

/**
 *
 * @author Hekta
 */
public class BukkitMCGiant extends BukkitMCCreature implements MCGiant {

	public BukkitMCGiant(Giant giant) {
		super(giant);
	}

	public BukkitMCGiant(AbstractionObject ao) {
		this((Giant) ao.getHandle());
	}

	@Override
	public Giant getHandle() {
		return (Giant) metadatable;
	}
}