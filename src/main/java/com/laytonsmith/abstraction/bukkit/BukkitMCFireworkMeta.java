package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCFireworkMeta;
import org.bukkit.inventory.meta.FireworkMeta;

public class BukkitMCFireworkMeta extends BukkitMCItemMeta implements MCFireworkMeta {

	FireworkMeta fm;
	public BukkitMCFireworkMeta(FireworkMeta im) {
		super(im);
		fm = im;
	}

	@Override
	public FireworkMeta asItemMeta() {
		return fm;
	}
	//TODO

}
