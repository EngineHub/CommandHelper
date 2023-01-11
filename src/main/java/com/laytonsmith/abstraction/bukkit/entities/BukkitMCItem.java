package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCItem;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

import java.util.UUID;

public class BukkitMCItem extends BukkitMCEntity implements MCItem {

	Item i;

	public BukkitMCItem(Entity i) {
		super(i);
		this.i = (Item) i;
	}

	@Override
	public MCItemStack getItemStack() {
		return new BukkitMCItemStack(i.getItemStack());
	}

	@Override
	public int getPickupDelay() {
		return i.getPickupDelay();
	}

	@Override
	public void setItemStack(MCItemStack stack) {
		i.setItemStack(((BukkitMCItemStack) stack).asItemStack());
	}

	@Override
	public void setPickupDelay(int delay) {
		i.setPickupDelay(delay);
	}

	@Override
	public UUID getOwner() {
		return i.getOwner();
	}

	@Override
	public void setOwner(UUID owner) {
		i.setOwner(owner);
	}

	@Override
	public UUID getThrower() {
		return i.getThrower();
	}

	@Override
	public void setThrower(UUID thrower) {
		i.setThrower(thrower);
	}

	@Override
	public boolean willDespawn() {
		return !i.isUnlimitedLifetime();
	}

	@Override
	public void setWillDespawn(boolean despawn) {
		i.setUnlimitedLifetime(!despawn);
	}
}
