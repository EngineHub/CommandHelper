package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.blocks.MCBeehive;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Beehive;
import org.bukkit.entity.Bee;
import org.bukkit.entity.EntitySnapshot;

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

	@Override
	public void addBees(int count) {
		try {
			EntitySnapshot beeSnapshot = Bukkit.getEntityFactory().createEntitySnapshot("{id:\"minecraft:bee\"}");
			World world;
			if(bh.isPlaced()) {
				world = bh.getWorld();
			} else {
				// use dummy world
				world = Bukkit.getWorlds().get(0);
			}
			for(int i = 0; i < count; i++) {
				bh.addEntity((Bee) beeSnapshot.createEntity(world));
			}
		} catch(Exception ignore) {
			// probably before 1.20.6
		}
	}

	@Override
	public int getEntityCount() {
		return bh.getEntityCount();
	}
}
