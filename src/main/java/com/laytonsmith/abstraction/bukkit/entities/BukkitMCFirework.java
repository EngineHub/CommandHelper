package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCFireworkMeta;
import com.laytonsmith.abstraction.bukkit.BukkitMCEntity;
import com.laytonsmith.abstraction.bukkit.BukkitMCFireworkMeta;
import com.laytonsmith.abstraction.entities.MCFirework;
import org.bukkit.entity.Firework;

public class BukkitMCFirework extends BukkitMCEntity implements MCFirework {

	Firework f;
	public BukkitMCFirework(Firework e) {
		super(e);
		f = e;
	}

	@Override
	public MCFireworkMeta getFireWorkMeta() {
		return new BukkitMCFireworkMeta(f.getFireworkMeta());
	}

	@Override
	public void setFireWorkMeta(MCFireworkMeta fm) {
		f.setFireworkMeta(((BukkitMCFireworkMeta) fm).asItemMeta());
	}

}
