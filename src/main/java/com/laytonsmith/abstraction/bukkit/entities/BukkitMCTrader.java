package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCMerchant;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;
import com.laytonsmith.abstraction.bukkit.BukkitMCMerchant;
import com.laytonsmith.abstraction.entities.MCTrader;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Entity;

public class BukkitMCTrader extends BukkitMCAgeable implements MCTrader {

	public BukkitMCTrader(Entity be) {
		super(be);
	}

	public BukkitMCTrader(AbstractionObject ao) {
		this((AbstractVillager) ao.getHandle());
	}

	@Override
	public AbstractVillager getHandle() {
		return (AbstractVillager) super.getHandle();
	}

	@Override
	public MCMerchant asMerchant() {
		AbstractVillager villager = getHandle();
		String title = getHandle().getCustomName() == null ? villager.getType().name() : villager.getCustomName();
		return new BukkitMCMerchant(villager, title);
	}

	@Override
	public MCInventory getInventory() {
		return new BukkitMCInventory(getHandle().getInventory());
	}
}
