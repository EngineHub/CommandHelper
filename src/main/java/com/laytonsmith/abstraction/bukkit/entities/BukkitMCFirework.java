package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionUtils;
import com.laytonsmith.abstraction.MCFireworkMeta;
import com.laytonsmith.abstraction.bukkit.BukkitMCEntity;
import com.laytonsmith.abstraction.bukkit.BukkitMCFireworkMeta;
import com.laytonsmith.abstraction.entities.MCFirework;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.entity.Firework;

public class BukkitMCFirework extends BukkitMCEntity implements MCFirework {

	@WrappedItem Firework f;

	public MCFireworkMeta getFireWorkMeta() {
		return AbstractionUtils.wrap(f.getFireworkMeta());
	}

	public void setFireWorkMeta(MCFireworkMeta fm) {
		f.setFireworkMeta(((BukkitMCFireworkMeta) fm).asItemMeta());
	}

}
