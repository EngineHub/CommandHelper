package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Silverfish;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCSilverfish;

/**
 *
 * @author Hekta
 */
public class BukkitMCSilverfish extends BukkitMCCreature implements MCSilverfish {

	public BukkitMCSilverfish(Silverfish silverfish) {
		super(silverfish);
	}

	public BukkitMCSilverfish(AbstractionObject ao) {
		this((Silverfish) ao.getHandle());
	}

	@Override
	public Silverfish getHandle() {
		return (Silverfish) metadatable;
	}
}