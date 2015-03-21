package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCChunk;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCEntity;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;

/**
 *
 * @author import
 */
public class BukkitMCChunk implements MCChunk {
	Chunk c;

	public BukkitMCChunk(Chunk c) {
		this.c = c;
	}
	
	@Override
	public int getX() {
		return c.getX();
	}

	@Override
	public int getZ() {
		return c.getZ();
	}

	@Override
	public MCEntity[] getEntities() {
		Entity[] entities = c.getEntities();
		MCEntity[] r = new MCEntity[entities.length];
		for (int i = 0 ; i < r.length ; i++) {
			r[i] = new BukkitMCEntity(entities[i]);
		}
		return r;
	}

	@Override
	public MCWorld getWorld() {
		return new BukkitMCWorld(c.getWorld());
	}
	
	@Override
	public Object getHandle() {
		return c;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof MCChunk ? this.c.equals(((BukkitMCChunk)o).c) : false;
	}

	@Override
	public int hashCode() {
		return c.hashCode();
}

	@Override
	public String toString() {
		return c.toString();
	}
}
