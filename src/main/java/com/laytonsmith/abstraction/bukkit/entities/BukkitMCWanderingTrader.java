package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCMerchant;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;
import com.laytonsmith.abstraction.bukkit.BukkitMCMerchant;
import com.laytonsmith.abstraction.entities.MCWanderingTrader;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.WanderingTrader;

public class BukkitMCWanderingTrader extends BukkitMCTrader implements MCWanderingTrader {

	public BukkitMCWanderingTrader(Entity wanderingtrader) {
		super(wanderingtrader);
	}

	@Override
	public WanderingTrader getHandle() {
		return (WanderingTrader) super.getHandle();
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
