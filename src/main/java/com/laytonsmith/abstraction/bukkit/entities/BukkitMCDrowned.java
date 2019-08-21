package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCDrowned;
import org.bukkit.entity.Entity;

public class BukkitMCDrowned extends BukkitMCZombie implements MCDrowned {

	public BukkitMCDrowned(Entity zombie) {
		super(zombie);
	}

}
