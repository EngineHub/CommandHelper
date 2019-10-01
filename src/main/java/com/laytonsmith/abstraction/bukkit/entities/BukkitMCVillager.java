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
	public int getLevel() {
		try {
			return getHandle().getVillagerLevel();
		} catch (NoSuchMethodError ex) {
			// 1.13
			return 1;
		}
	}

	@Override
	public void setLevel(int level) {
		try {
			getHandle().setVillagerLevel(level);
		} catch (NoSuchMethodError ex) {
			// 1.13
		}
	}

	@Override
	public int getExperience() {
		try {
			return getHandle().getVillagerExperience();
		} catch (NoSuchMethodError ex) {
			// 1.13
			return 0;
		}
	}

	@Override
	public void setExperience(int exp) {
		try {
			getHandle().setVillagerExperience(exp);
		} catch (NoSuchMethodError ex) {
			// 1.13
		}
	}

	@Override
	public MCMerchant asMerchant() {
		Villager villager = getHandle();
		String title = villager.getCustomName() == null ? getHandle().getProfession().name() : villager.getCustomName();
		return new BukkitMCMerchant(villager, title);
	}

	@Override
	public MCInventory getInventory() {
		return new BukkitMCInventory(getHandle().getInventory());
	}
}
