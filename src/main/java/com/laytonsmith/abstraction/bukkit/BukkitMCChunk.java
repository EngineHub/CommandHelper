
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.bukkit.entities.BukkitMCEntity;
import com.laytonsmith.abstraction.MCChunk;
import com.laytonsmith.abstraction.entities.MCEntity;
import com.laytonsmith.abstraction.MCWorld;
import java.util.ArrayList;
import java.util.List;
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
	
	public int getX() {
		return c.getX();
	}

	public int getZ() {
		return c.getZ();
	}

	public List<MCEntity> getEntities() {
		List<MCEntity> entities = new ArrayList<MCEntity>();
		for (Entity entity : c.getEntities()) {
			entities.add(BukkitConvertor.BukkitGetCorrectEntity(entity));
		}
		return entities;
	}

	public MCWorld getWorld() {
		return new BukkitMCWorld(c.getWorld());
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
