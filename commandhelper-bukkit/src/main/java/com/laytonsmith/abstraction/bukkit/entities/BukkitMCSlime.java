package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCSlime;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Slime;

/**
 *
 * @author Hekta
 */
public class BukkitMCSlime extends BukkitMCLivingEntity implements MCSlime {

	public BukkitMCSlime(Entity slime) {
		super(slime);
	}

	public BukkitMCSlime(AbstractionObject ao) {
		this((Slime) ao.getHandle());
	}

	@Override
	public int getSize() {
		return ((Slime)getHandle()).getSize();
	}

	@Override
	public void setSize(int size) {
		((Slime)getHandle()).setSize(size);
	}
}