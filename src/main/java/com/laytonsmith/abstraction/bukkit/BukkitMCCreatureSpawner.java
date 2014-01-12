
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCCreatureSpawner;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlockState;
import com.laytonsmith.abstraction.enums.MCEntityType;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

/**
 *
 * @author Layton
 */
public class BukkitMCCreatureSpawner extends BukkitMCBlockState implements MCCreatureSpawner{
	
	CreatureSpawner cs;
	public BukkitMCCreatureSpawner(CreatureSpawner cs){
		super(cs);
		this.cs = cs;
	}

	@Override
	public MCEntityType getSpawnedType() {
		return MCEntityType.valueOf(cs.getSpawnedType().name());
	}

	@Override
	public void setSpawnedType(MCEntityType type) {
		cs.setSpawnedType(EntityType.valueOf(type.name()));
	}
	
}
