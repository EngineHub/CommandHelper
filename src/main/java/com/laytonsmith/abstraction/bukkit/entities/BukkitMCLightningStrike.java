package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCLightningStrike;

import org.bukkit.entity.LightningStrike;

/**
 *
 * @author Jim
 */
public class BukkitMCLightningStrike extends BukkitMCEntity implements MCLightningStrike {

	public BukkitMCLightningStrike(LightningStrike lightning) {
		super(lightning);
	}

	public BukkitMCLightningStrike(AbstractionObject ao) {
		this((LightningStrike) ao.getHandle());
	}

	@Override
	public LightningStrike getHandle() {
		return (LightningStrike) metadatable;
	}

	public boolean isEffect() {
		return getHandle().isEffect();
	}
}