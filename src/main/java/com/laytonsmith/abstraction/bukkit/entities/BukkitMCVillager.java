package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCMerchant;
import com.laytonsmith.abstraction.bukkit.BukkitMCMerchant;
import com.laytonsmith.abstraction.entities.MCVillager;
import com.laytonsmith.abstraction.enums.MCProfession;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCProfession;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;

public class BukkitMCVillager extends BukkitMCTrader implements MCVillager {

	public BukkitMCVillager(Entity villager) {
		super(villager);
	}

	public BukkitMCVillager(AbstractionObject ao) {
		this((Villager) ao.getHandle());
	}

	@Override
	public Villager getHandle() {
		return (Villager) super.getHandle();
	}

	@Override
	public MCProfession getProfession() {
		return BukkitMCProfession.valueOfConcrete(getHandle().getProfession());
	}

	@Override
	public void setProfession(MCProfession profession) {
		getHandle().setProfession((Villager.Profession) profession.getConcrete());
	}

	@Override
	public MCMerchant asMerchant() {
		Villager villager = getHandle();
		String title = villager.getCustomName() == null ? getHandle().getProfession().name() : villager.getCustomName();
		return new BukkitMCMerchant(villager, title);
	}
}
