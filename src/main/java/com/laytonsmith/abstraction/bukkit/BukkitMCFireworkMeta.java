package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCFireworkMeta;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.inventory.meta.FireworkMeta;

public class BukkitMCFireworkMeta extends BukkitMCItemMeta implements MCFireworkMeta {

	@WrappedItem FireworkMeta fm;

	@Override
	public FireworkMeta asItemMeta() {
		return fm;
	}
	//TODO

}
