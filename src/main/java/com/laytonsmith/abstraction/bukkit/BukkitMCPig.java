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

	@Override
	public boolean isSaddled() {
		return p.hasSaddle();
	}

	@Override
	public void setSaddled(boolean saddled) {
		p.setSaddle(saddled);
	}
}
