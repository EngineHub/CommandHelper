package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCFireworkMeta;
import com.laytonsmith.abstraction.bukkit.BukkitMCFireworkMeta;
import com.laytonsmith.abstraction.entities.MCFirework;
import org.bukkit.entity.Firework;

public class BukkitMCFirework extends BukkitMCEntity implements MCFirework {

	public BukkitMCFirework(Firework firework) {
		super(firework);
	}

	public BukkitMCFirework(AbstractionObject ao) {
		this((Firework) ao.getHandle());
	}

	@Override
	public Firework getHandle() {
		return (Firework) metadatable;
	}

	public MCFireworkMeta getFireWorkMeta() {
		return new BukkitMCFireworkMeta(getHandle().getFireworkMeta());
	}

	public void setFireWorkMeta(MCFireworkMeta fm) {
		getHandle().setFireworkMeta(((BukkitMCFireworkMeta) fm).asItemMeta());
	}

}
