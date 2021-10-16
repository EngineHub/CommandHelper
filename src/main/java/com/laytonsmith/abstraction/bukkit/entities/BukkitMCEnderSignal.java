package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.entities.MCEnderSignal;
import org.bukkit.Location;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class BukkitMCEnderSignal extends BukkitMCEntity implements MCEnderSignal {

	EnderSignal es;

	public BukkitMCEnderSignal(Entity e) {
		super(e);
		this.es = (EnderSignal) e;
	}

	@Override
	public int getDespawnTicks() {
		return es.getDespawnTimer();
	}

	@Override
	public void setDespawnTicks(int ticks) {
		es.setDespawnTimer(ticks);
	}

	@Override
	public boolean getDropItem() {
		return es.getDropItem();
	}

	@Override
	public void setDropItem(boolean drop) {
		es.setDropItem(drop);
	}

	@Override
	public MCLocation getTargetLocation() {
		return new BukkitMCLocation(es.getTargetLocation());
	}

	@Override
	public void setTargetLocation(MCLocation loc) {
		es.setTargetLocation((Location) loc.getHandle());
	}

	@Override
	public MCItemStack getItem() {
		return new BukkitMCItemStack(es.getItem());
	}

	@Override
	public void setItem(MCItemStack stack) {
		if(stack == null) {
			es.setItem(null);
		} else {
			es.setItem((ItemStack) stack.getHandle());
		}
	}
}
