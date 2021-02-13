package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.entities.MCItemProjectile;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ThrowableProjectile;
import org.bukkit.inventory.ItemStack;

public class BukkitMCItemProjectile extends BukkitMCProjectile implements MCItemProjectile {

	public BukkitMCItemProjectile(Entity be) {
		super(be);
	}

	@Override
	public MCItemStack getItem() {
		return new BukkitMCItemStack(((ThrowableProjectile) getHandle()).getItem());
	}

	@Override
	public void setItem(MCItemStack item) {
		((ThrowableProjectile) getHandle()).setItem((ItemStack) item.getHandle());
	}
}
