package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCSpawnEggMeta;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCEntityType;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.meta.SpawnEggMeta;

public class BukkitMCSpawnEggMeta extends BukkitMCItemMeta implements MCSpawnEggMeta {

	SpawnEggMeta sem;

	public BukkitMCSpawnEggMeta(SpawnEggMeta meta) {
		super(meta);
		this.sem = meta;
	}

	@Override
	public MCEntityType getSpawnedType() {
		EntityType type = sem.getSpawnedType();
		if(type == null) {
			return null;
		}
		return BukkitMCEntityType.valueOfConcrete(sem.getSpawnedType());
	}

	@Override
	public void setSpawnedType(MCEntityType type) {
		sem.setSpawnedType(((BukkitMCEntityType) type).getConcrete());
	}
}
