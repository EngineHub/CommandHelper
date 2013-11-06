package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.bukkit.BukkitMCAnimalTamer;
import com.laytonsmith.abstraction.entities.MCTameable;
import com.laytonsmith.abstraction.MCAnimalTamer;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Tameable;

/**
 *
 * @author layton
 */
public abstract class BukkitMCTameable extends BukkitMCAgeable implements MCTameable {

	protected Tameable tameable;

	public BukkitMCTameable(Tameable t) {
		super((Ageable) t);
		this.tameable = t;
	}

	@Override
	public Ageable getHandle() {
		return (Ageable) metadatable;
	}

	public boolean isTamed() {
		return tameable.isTamed();
	}

	public void setTamed(boolean bln) {
		tameable.setTamed(bln);
	}

	public MCAnimalTamer getOwner() {
		if (tameable.getOwner() == null) {
			return null;
		} else {
			return new BukkitMCAnimalTamer(tameable.getOwner());
		}
	}

	public void setOwner(MCAnimalTamer tamer) {
		tameable.setOwner((AnimalTamer) tamer.getHandle());
	}
}