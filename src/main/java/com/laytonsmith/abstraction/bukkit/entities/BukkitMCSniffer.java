package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCSniffer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Sniffer;

public class BukkitMCSniffer extends BukkitMCAnimal implements MCSniffer {

	Sniffer s;

	public BukkitMCSniffer(Entity e) {
		super(e);
		this.s = (Sniffer) e;
	}

}
