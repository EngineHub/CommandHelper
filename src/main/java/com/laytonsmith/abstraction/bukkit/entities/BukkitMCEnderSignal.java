package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.entities.MCEnderSignal;
import org.bukkit.Location;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.Entity;

public class BukkitMCEnderSignal extends BukkitMCEntity implements MCEnderSignal {

	EnderSignal es;

	public BukkitMCEnderSignal(Entity e) {
		super(e);
		this.es = (EnderSignal) e;
	}

	@Override
	public int getDespawnTicks() {
		return es.getDespawnTimer();
	}

	@Override
	public void setDespawnTicks(int ticks) {
		try {
			es.setDespawnTimer(ticks);
		} catch (NoSuchMethodError ex) {
			// probably prior to 1.12.2
		}
	}

	@Override
	public boolean getDropItem() {
		return es.getDropItem();
	}

	@Override
	public void setDropItem(boolean drop) {
		try {
			es.setDropItem(drop);
		} catch (NoSuchMethodError ex) {
			// probably prior to 1.12.2
		}
	}

	@Override
	public MCLocation getTargetLocation() {
		return new BukkitMCLocation(es.getTargetLocation());
	}

	@Override
	public void setTargetLocation(MCLocation loc) {
		try {
			es.setTargetLocation((Location) loc.getHandle());
		} catch (NoSuchMethodError ex) {
			// probably prior to 1.12.2
		}
	}
}
