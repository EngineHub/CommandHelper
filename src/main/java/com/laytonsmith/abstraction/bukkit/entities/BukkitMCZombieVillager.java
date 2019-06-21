package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCZombieVillager;
import com.laytonsmith.abstraction.enums.MCProfession;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCProfession;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.ZombieVillager;

public class BukkitMCZombieVillager extends BukkitMCZombie implements MCZombieVillager {

	ZombieVillager zv;

	public BukkitMCZombieVillager(Entity ent) {
		super(ent);
		zv = (ZombieVillager) ent;
	}

	@Override
	public MCProfession getProfession() {
		return BukkitMCProfession.valueOfConcrete(zv.getVillagerProfession());
	}

	@Override
	public void setProfession(MCProfession profession) {
		zv.setVillagerProfession((Villager.Profession) profession.getConcrete());
	}

}
