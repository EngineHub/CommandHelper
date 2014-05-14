package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.entities.MCPig;
import org.bukkit.entity.Pig;

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
