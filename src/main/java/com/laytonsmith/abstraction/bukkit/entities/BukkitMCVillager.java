package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCVillager;
import com.laytonsmith.abstraction.enums.MCProfession;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCProfession;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;

/**
 *
 * @author Hekta
 */
public class BukkitMCVillager extends BukkitMCAgeable implements MCVillager {

	public BukkitMCVillager(Entity villager) {
		super(villager);
	}

	public BukkitMCVillager(AbstractionObject ao) {
		this((Villager) ao.getHandle());
	}

	@Override
	public Villager getHandle() {
		return (Villager)super.getHandle();
	}

	@Override
	public MCProfession getProfession() {
		return BukkitMCProfession.getConvertor().getAbstractedEnum(getHandle().getProfession());
	}

	@Override
	public void setProfession(MCProfession profession) {
		getHandle().setProfession(BukkitMCProfession.getConvertor().getConcreteEnum(profession));
	}
}