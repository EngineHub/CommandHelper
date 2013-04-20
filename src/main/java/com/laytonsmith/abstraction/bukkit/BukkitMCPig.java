package com.laytonsmith.abstraction.bukkit;

import org.bukkit.entity.Pig;

import com.laytonsmith.abstraction.entities.MCPig;

/**
 * 
 * @author jb_aero
 */
public class BukkitMCPig extends BukkitMCAgeable implements MCPig {

	Pig p;
	public BukkitMCPig(Pig be) {
		super(be);
		p = be;
	}

	public boolean isSaddled() {
		return p.hasSaddle();
	}

	public void setSaddled(boolean saddled) {
		p.setSaddle(saddled);
	}
}
