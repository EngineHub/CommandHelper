
package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.bukkit.BukkitMCLivingEntity;
import com.laytonsmith.abstraction.entities.MCCreeper;
import org.bukkit.entity.Creeper;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
public class BukkitMCCreeper extends BukkitMCLivingEntity implements MCCreeper {
	Creeper creeper;
	
	public BukkitMCCreeper(Creeper c) {
		super(c);
		creeper = c;
	}
	
	public BukkitMCCreeper(AbstractionObject ao) {
		this ((Creeper) ao.getHandle());
	}

	@Override
	public boolean isPowered() {
		return creeper.isPowered();
	}

	@Override
	public void setPowered(boolean powered) {
		creeper.setPowered(powered);
	}
}
