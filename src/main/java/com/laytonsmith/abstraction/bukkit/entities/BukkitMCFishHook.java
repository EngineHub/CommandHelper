package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCFishHook;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;

public class BukkitMCFishHook extends BukkitMCProjectile implements MCFishHook {

	FishHook f;

	public BukkitMCFishHook(Entity e) {
		super(e);
		f = (FishHook) e;
	}

}
