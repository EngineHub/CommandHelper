package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCMerchant;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;
import com.laytonsmith.abstraction.bukkit.BukkitMCMerchant;
import com.laytonsmith.abstraction.entities.MCVillager;
import com.laytonsmith.abstraction.enums.MCProfession;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCProfession;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;

public class BukkitMCVillager extends BukkitMCAgeable implements MCVillager {

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
		return BukkitMCProfession.getConvertor().getAbstractedEnum(getHandle().getProfession());
	}

	@Override
	public void setProfession(MCProfession profession) {
		getHandle().setProfession(BukkitMCProfession.getConvertor().getConcreteEnum(profession));
	}

	@Override
	public MCInventory getInventory() {
		return new BukkitMCInventory(getHandle().getInventory());
	}

	@Override
	public MCMerchant asMerchant() {
		return new BukkitMCMerchant(getHandle());
	}

	@Override
	public int getRiches() {
		return getHandle().getRiches();
	}

	@Override
	public void setRiches(int riches) {
		getHandle().setRiches(riches);
	}
}
