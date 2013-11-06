package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCAgeable;
import org.bukkit.entity.Ageable;

/**
 * 
 * @author jb_aero
 */
public abstract class BukkitMCAgeable extends BukkitMCCreature implements MCAgeable {

	public BukkitMCAgeable(Ageable ageable) {
		super(ageable);
	}

	@Override
	public Ageable getHandle() {
		return (Ageable) metadatable;
	}

	public boolean getCanBreed() {
		return getHandle().canBreed();
	}

	public void setCanBreed(boolean breed) {
		getHandle().setBreed(breed);
	}

	public int getAge() {
		return getHandle().getAge();
	}

	public void setAge(int age) {
		getHandle().setAge(age);
	}

	public boolean getAgeLock() {
		return getHandle().getAgeLock();
	}

	public void setAgeLock(boolean lock) {
		getHandle().setAgeLock(lock);
	}

	public boolean isAdult() {
		return getHandle().isAdult();
	}

	public void setAdult() {
		getHandle().setAdult();
	}

	public void setBaby() {
		getHandle().setBaby();
	}
}