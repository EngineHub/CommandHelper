package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCPufferfish;
import org.bukkit.entity.Entity;
import org.bukkit.entity.PufferFish;

public class BukkitMCPufferfish extends BukkitMCLivingEntity implements MCPufferfish {

	private final PufferFish p;

	public BukkitMCPufferfish(Entity be) {
		super(be);
		this.p = (PufferFish) be;
	}

	@Override
	public int getPuffState() {
		return this.p.getPuffState();
	}

	@Override
	public void setPuffState(int state) {
		this.p.setPuffState(state);
	}
}
