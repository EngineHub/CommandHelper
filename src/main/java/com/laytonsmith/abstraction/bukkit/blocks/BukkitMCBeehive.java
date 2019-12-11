package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.blocks.MCBeehive;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import org.bukkit.Location;
import org.bukkit.block.Beehive;

public class BukkitMCBeehive extends BukkitMCBlockState implements MCBeehive {

	Beehive bh;

	public BukkitMCBeehive(Beehive hive) {
		super(hive);
		this.bh = hive;
	}

	@Override
	public Beehive getHandle() {
		return bh;
	}

	@Override
	public MCLocation getFlowerLocation() {
		Location loc = bh.getFlower();
		if(loc == null) {
			return null;
		}
		return new BukkitMCLocation(loc);
	}

	@Override
	public void setFlowerLocation(MCLocation loc) {
		if(loc == null) {
			bh.setFlower(null);
		} else {
			bh.setFlower((Location) loc.getHandle());
		}
	}
}
