package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCPiglinBrute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.PiglinBrute;

public class BukkitMCPiglinBrute extends BukkitMCLivingEntity implements MCPiglinBrute {

	public BukkitMCPiglinBrute(Entity ent) {
		super(ent);
	}

	public BukkitMCPiglinBrute(AbstractionObject ao) {
		this((PiglinBrute) ao.getHandle());
	}

	@Override
	public PiglinBrute getHandle() {
		return (PiglinBrute) super.getHandle();
	}

}
