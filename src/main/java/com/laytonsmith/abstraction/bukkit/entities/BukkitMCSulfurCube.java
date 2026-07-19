package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCSulfurCube;
import org.bukkit.entity.Entity;
import org.bukkit.entity.SulfurCube;

public class BukkitMCSulfurCube extends BukkitMCLivingEntity implements MCSulfurCube {

	public BukkitMCSulfurCube(Entity sulfurCube) {
		super(sulfurCube);
	}

	@Override
	public int getSize() {
		return ((SulfurCube) getHandle()).getSize();
	}

	@Override
	public void setSize(int size) {
		((SulfurCube) getHandle()).setSize(size);
	}
}
