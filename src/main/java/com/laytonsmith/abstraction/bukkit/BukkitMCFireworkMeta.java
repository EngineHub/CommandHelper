package com.laytonsmith.abstraction.bukkit;

import org.bukkit.inventory.meta.FireworkMeta;
import com.laytonsmith.abstraction.MCFireworkMeta;

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
