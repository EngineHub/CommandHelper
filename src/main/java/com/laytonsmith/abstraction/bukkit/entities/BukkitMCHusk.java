package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCHusk;
import org.bukkit.entity.Entity;

public class BukkitMCHusk extends BukkitMCZombie implements MCHusk {

	public BukkitMCHusk(Entity zombie) {
		super(zombie);
	}

}
