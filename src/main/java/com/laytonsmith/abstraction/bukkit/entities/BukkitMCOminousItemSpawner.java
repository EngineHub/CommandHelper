package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.entities.MCOminousItemSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.OminousItemSpawner;
import org.bukkit.inventory.ItemStack;

public class BukkitMCOminousItemSpawner extends BukkitMCEntity implements MCOminousItemSpawner {

	OminousItemSpawner spawner;

	public BukkitMCOminousItemSpawner(Entity e) {
		super(e);
		this.spawner = (OminousItemSpawner) e;
	}

	@Override
	public MCItemStack getItem() {
		if(this.spawner.getItem() == null) {
			return null;
		}
		return new BukkitMCItemStack(this.spawner.getItem());
	}

	@Override
	public void setItem(MCItemStack item) {
		if(item == null) {
			this.spawner.setItem(null);
		} else {
			this.spawner.setItem((ItemStack) item.getHandle());
		}
	}

	@Override
	public long getDelay() {
		return this.spawner.getSpawnItemAfterTicks();
	}

	@Override
	public void setDelay(long delay) {
		this.spawner.setSpawnItemAfterTicks(delay);
	}
}
