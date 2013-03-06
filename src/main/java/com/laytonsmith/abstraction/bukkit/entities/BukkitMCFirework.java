package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Firework;

import com.laytonsmith.abstraction.MCFireworkMeta;
import com.laytonsmith.abstraction.bukkit.BukkitMCEntity;
import com.laytonsmith.abstraction.bukkit.BukkitMCFireworkMeta;
import com.laytonsmith.abstraction.entities.MCFirework;

public class BukkitMCFirework extends BukkitMCEntity implements MCFirework {

	Firework f;
	public BukkitMCFirework(Firework e) {
		super(e);
		f = e;
	}

	public MCFireworkMeta getFireWorkMeta() {
		return new BukkitMCFireworkMeta(f.getFireworkMeta());
	}

	public void setFireWorkMeta(MCFireworkMeta fm) {
		f.setFireworkMeta(((BukkitMCFireworkMeta) fm).asItemMeta());
	}

}
