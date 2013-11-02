package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Snowman;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCSnowman;

/**
 *
 * @author Hekta
 */
public class BukkitMCSnowman extends BukkitMCCreature implements MCSnowman {

	public BukkitMCSnowman(Snowman snowman) {
		super(snowman);
	}

	public BukkitMCSnowman(AbstractionObject ao) {
		this((Snowman) ao.getHandle());
	}

	@Override
	public Snowman getHandle() {
		return (Snowman) metadatable;
	}
}