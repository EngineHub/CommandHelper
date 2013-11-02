package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Villager;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCVillager;
import com.laytonsmith.abstraction.enums.MCProfession;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCProfession;

/**
 *
 * @author Hekta
 */
public class BukkitMCVillager extends BukkitMCAgeable implements MCVillager {

	public BukkitMCVillager(Villager villager) {
		super(villager);
	}

	public BukkitMCVillager(AbstractionObject ao) {
		this((Villager) ao.getHandle());
	}

	@Override
	public Villager getHandle() {
		return (Villager) metadatable;
	}

	public MCProfession getProfession() {
		return BukkitMCProfession.getConvertor().getAbstractedEnum(getHandle().getProfession());
	}

	public void setProfession(MCProfession profession) {
		getHandle().setProfession(BukkitMCProfession.getConvertor().getConcreteEnum(profession));
	}
}