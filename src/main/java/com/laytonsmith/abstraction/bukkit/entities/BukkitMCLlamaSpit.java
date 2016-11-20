package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCLlamaSpit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LlamaSpit;

public class BukkitMCLlamaSpit extends BukkitMCProjectile implements MCLlamaSpit {

	LlamaSpit ls;

	public BukkitMCLlamaSpit(Entity ent) {
		super(ent);
		ls = (LlamaSpit) ent;
	}

}