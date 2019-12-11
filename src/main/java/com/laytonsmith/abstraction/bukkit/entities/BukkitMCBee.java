package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.entities.MCBee;
import org.bukkit.Location;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;

public class BukkitMCBee extends BukkitMCLivingEntity implements MCBee {

	Bee b;

	public BukkitMCBee(Entity e) {
		super(e);
		this.b = (Bee) e;
	}

	@Override
	public MCLocation getHiveLocation() {
		Location loc = b.getHive();
		if(loc == null) {
			return null;
		}
		return new BukkitMCLocation(loc);
	}

	@Override
	public void setHiveLocation(MCLocation loc) {
		if(loc == null) {
			b.setHive(null);
		} else {
			b.setHive((Location) loc.getHandle());
		}
	}

	@Override
	public MCLocation getFlowerLocation() {
		Location loc = b.getFlower();
		if(loc == null) {
			return null;
		}
		return new BukkitMCLocation(loc);
	}

	@Override
	public void setFlowerLocation(MCLocation loc) {
		if(loc == null) {
			b.setFlower(null);
		} else {
			b.setFlower((Location) loc.getHandle());
		}
	}

	@Override
	public boolean hasNectar() {
		return b.hasNectar();
	}

	@Override
	public void setHasNectar(boolean nectar) {
		b.setHasNectar(nectar);
	}

	@Override
	public boolean hasStung() {
		return b.hasStung();
	}

	@Override
	public void setHasStung(boolean stung) {
		b.setHasStung(stung);
	}

	@Override
	public int getAnger() {
		return b.getAnger();
	}

	@Override
	public void setAnger(int ticks) {
		b.setAnger(ticks);
	}
}
