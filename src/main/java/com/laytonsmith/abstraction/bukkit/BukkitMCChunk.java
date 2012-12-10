/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCChunk;
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

	public Object getHandle() {
		return c;
	}
	
	
}
