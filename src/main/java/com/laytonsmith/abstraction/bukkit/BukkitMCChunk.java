
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCChunk;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCWorld;
import org.bukkit.Chunk;

/**
 *
 * @author import
 */
public class BukkitMCChunk implements MCChunk {
	Chunk c;

	public BukkitMCChunk(Chunk c) {
		this.c = c;
	}
	
	public int getX() {
		return c.getX();
	}

	public int getZ() {
		return c.getZ();
	}

	public MCEntity[] getEntities() {
		MCEntity[] ret = new MCEntity[c.getEntities().length];
		for (int i=0; i < c.getEntities().length; i++) {
			ret[i] = new BukkitMCEntity(c.getEntities()[i]);
		}
		return ret;
	}

	public MCWorld getWorld() {
		return new BukkitMCWorld(c.getWorld());
	}

	public MCEntity[] getEntities() {
		MCEntity[] ret = new MCEntity[c.getEntities().length];
		for (int i=0; i < c.getEntities().length; i++) {
			ret[i] = new BukkitMCEntity(c.getEntities()[i]);
		}
		return ret;
	}
	
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
