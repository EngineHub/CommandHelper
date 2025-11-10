package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCAnimalTamer;
import org.bukkit.entity.AnimalTamer;

import java.util.UUID;

public class BukkitMCAnimalTamer implements MCAnimalTamer {

	AnimalTamer at;

	public BukkitMCAnimalTamer(AnimalTamer at) {
		this.at = at;
	}

	public BukkitMCAnimalTamer(AbstractionObject a) {
		this((AnimalTamer) null);
		if(a instanceof MCAnimalTamer) {
			this.at = ((AnimalTamer) a.getHandle());
		} else {
			throw new ClassCastException();
		}
	}

	@Override
	public Object getHandle() {
		return at;
	}

	public AnimalTamer _tamer() {
		return at;
	}

	@Override
	public String toString() {
		return at.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof BukkitMCAnimalTamer && at.equals(((BukkitMCAnimalTamer) obj).at);
	}

	@Override
	public int hashCode() {
		return at.hashCode();
	}

	@Override
	public String getName() {
		return at.getName();
	}

	@Override
	public UUID getUniqueID() {
		return at.getUniqueId();
	}
}
