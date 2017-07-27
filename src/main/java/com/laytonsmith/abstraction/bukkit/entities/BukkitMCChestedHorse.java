package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCChestedHorse;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Entity;

public class BukkitMCChestedHorse extends BukkitMCAbstractHorse implements MCChestedHorse {

	ChestedHorse ch;

	public BukkitMCChestedHorse(Entity t) {
		super(t);
		ch = (ChestedHorse) t;
	}

	@Override
	public boolean hasChest() {
		return ch.isCarryingChest();
	}

	@Override
	public void setHasChest(boolean hasChest) {
		ch.setCarryingChest(hasChest);
	}
}
