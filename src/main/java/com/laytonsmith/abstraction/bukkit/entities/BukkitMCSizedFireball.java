package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.entities.MCItemProjectile;
import org.bukkit.entity.Entity;
import org.bukkit.entity.SizedFireball;
import org.bukkit.inventory.ItemStack;

public class BukkitMCSizedFireball extends BukkitMCFireball implements MCItemProjectile {

	public BukkitMCSizedFireball(Entity be) {
		super(be);
	}

	@Override
	public MCItemStack getItem() {
		return new BukkitMCItemStack(((SizedFireball) getHandle()).getDisplayItem());
	}

	@Override
	public void setItem(MCItemStack item) {
		((SizedFireball) getHandle()).setDisplayItem((ItemStack) item.getHandle());
	}
}
