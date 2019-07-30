package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCWanderingTrader;
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
}
